/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Li Song
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
//@formatter:on
package org.lisoft.lsml.util;

import java.util.*;

import javax.inject.*;

import org.lisoft.lsml.messages.*;

import javafx.beans.binding.ObjectBinding;

/**
 * This class models an command stack that can be used for undo etc (see: Command Pattern). It will automatically reset
 * the stack if a new garage is loaded.
 *
 * @author Li Song
 */
public class CommandStack {
    /**
     * The {@link Command} class represents an action that can be (un)done. Undoing the action will restore the state of
     * affected object to that before the {@link Command} was done.
     *
     * @author Li Song
     */
    public interface Command {

        /**
         * @return A {@link String} containing a (short) human readable description of this action.
         */
        String describe();

        /**
         * Will 'do' this operation
         *
         * @throws Exception
         *             If the operation failed.
         */
        void apply() throws Exception;

        /**
         * Checks if two operations can be coalesced into one. By definition an object can't coalesce with itself.
         * <p>
         * If this function returns true, then the previous operation may be quietly undone and this operation replace
         * it. I.e. premises for the operation to succeed may have changed from construction time to the time point when
         * apply is called.
         * </p>
         *
         * @param aOperation
         *            The {@link Command} to check with.
         * @return <code>true</code> if <code>this</code> can coalescele with aOperation.
         */
        default boolean canCoalesce(Command aOperation) {
            return false;
        }

        /**
         * Will undo this action.
         *
         */
        void undo();
    }

    /**
     * This class models an operation that should be considered as one but actually consists of many smaller operations
     * that are all performed in order as one transaction.
     *
     * @author Li Song
     */
    public abstract static class CompositeCommand implements Command {
        protected final MessageBuffer messageBuffer = new MessageBuffer();
        private final List<Command> commands = new ArrayList<>();
        private final String desciption;
        private transient boolean isPrepared = false;
        private final MessageDelivery messageTarget;

        public CompositeCommand(String aDescription, MessageDelivery aMessageTarget) {
            desciption = aDescription;
            messageTarget = aMessageTarget;
        }

        public void addOp(Command anOperation) {
            commands.add(anOperation);
        }

        @Override
        public void apply() throws Exception {
            if (!isPrepared) {
                buildCommand();
                isPrepared = true;
            }

            final ListIterator<Command> it = commands.listIterator();
            while (it.hasNext()) {
                try {
                    it.next().apply();
                }
                catch (final Throwable t) {
                    // Roll back the transaction
                    it.previous();
                    undoAll(it);
                    throw t;
                }
            }

            messageBuffer.deliverTo(messageTarget);
        }

        @Override
        public String describe() {
            return desciption;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CompositeCommand)) {
                return false;
            }
            final CompositeCommand other = (CompositeCommand) obj;
            return commands.equals(other.commands);
        }

        @Override
        public int hashCode() {
            return commands.hashCode();
        }

        @Override
        public void undo() {
            if (!isPrepared) {
                throw new IllegalStateException("Undo called before apply!");
            }

            // Do it in the "right" i.e. backwards order
            final ListIterator<Command> it = commands.listIterator(commands.size());
            undoAll(it);

            messageBuffer.deliverTo(messageTarget);
        }

        /**
         * The user should implement this to create the operation. Will be called only once, immediately before the
         * first time the operation is applied.
         *
         * @throws Exception
         *             If for some reason the command failed to build.
         */
        protected abstract void buildCommand() throws Exception;

        private void undoAll(final ListIterator<Command> it) {
            while (it.hasPrevious()) {
                it.previous().undo();
            }
        }
    }

    private final List<Command> cmdHistory = new ArrayList<>();
    private int currentCmd = -1;
    private final int maxHistory;

    private final ObjectBinding<Command> nextRedoProp = new ObjectBinding<Command>() {
        @Override
        protected Command computeValue() {
            return nextRedo();
        }
    };

    private final ObjectBinding<Command> nextUndoProp = new ObjectBinding<Command>() {
        @Override
        protected Command computeValue() {
            return nextUndo();
        }
    };

    /**
     * Creates a new {@link CommandStack} that listens on the given {@link MessageXBar} for garage resets and has the
     * given undo depth.
     *
     * @param aUndoDepth
     *            The number of undo levels allowed.
     */
    @Inject
    public CommandStack(@Named("undodepth") int aUndoDepth) {
        maxHistory = aUndoDepth;
    }

    public Command nextRedo() {
        if (currentCmd + 1 >= cmdHistory.size()) {
            return null;
        }

        return cmdHistory.get(currentCmd + 1);
    }

    public ObjectBinding<Command> nextRedoProperty() {
        return nextRedoProp;
    }

    public Command nextUndo() {
        if (currentCmd < 0) {
            return null;
        }
        return cmdHistory.get(currentCmd);
    }

    public ObjectBinding<Command> nextUndoProperty() {
        return nextUndoProp;
    }

    public void pushAndApply(Command aCmd) throws Exception {
        // Perform automatic coalescing
        final int cmdBeforeCoalesce = currentCmd;
        while (nextUndo() != null && nextUndo().canCoalesce(aCmd)) {
            undo();
        }

        try {
            aCmd.apply();
        }
        catch (final Exception throwable) {
            // Undo the coalescing if the new operation threw.
            while (currentCmd != cmdBeforeCoalesce && nextRedo() != null) {
                redo();
            }
            throw throwable;
        }
        while (currentCmd < cmdHistory.size() - 1) {
            // Previously undone actions in the list
            cmdHistory.remove(cmdHistory.size() - 1);
        }
        cmdHistory.add(aCmd);
        currentCmd = cmdHistory.size() - 1;

        while (cmdHistory.size() > maxHistory) {
            cmdHistory.remove(0);
            currentCmd--;
        }
        // FIXME: Unit test the bindings functionality.
        updateBindings(); // FIXME: does this need to be in a try-catch on
        // apply?
    }

    public void redo() {
        final Command cmd = nextRedo();
        if (null != cmd) {
            try {
                cmd.apply();

            }
            catch (final Exception e) {
                // If the apply succeeded once, and has been undone. In must
                // succeed again.
                throw new RuntimeException("Previously succeeded command failed when redone", e);
            }
            currentCmd++;
        }
        updateBindings(); // FIXME: does this need to be in a try-catch on
        // apply?
    }

    public void undo() {
        final Command cmd = nextUndo();
        if (null != cmd) {
            cmd.undo();
            currentCmd--;
        }
        updateBindings(); // FIXME: does this need to be in a try-catch on undo?
    }

    private void updateBindings() {
        nextRedoProp.invalidate();
        nextUndoProp.invalidate();
    }
}
