/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Emily Björk
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.lisoft.lsml.messages.MessageBuffer;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.loadout.EquipException;

import javafx.beans.binding.ObjectBinding;

/**
 * This class models an command stack that can be used for undo etc (see: Command Pattern). It will automatically reset
 * the stack if a new garage is loaded.
 * 
 * @author Emily Björk
 */
public class CommandStack {
    /**
     * The {@link Command} class represents an action that can be (un)done. Undoing the action will restore the state of
     * affected object to that before the {@link Command} was done.
     * 
     * @author Emily Björk
     */
    public static abstract class Command {

        /**
         * Checks if two operations can be coalesceled into one. By definition an object can't coalescele with itself.
         * <p>
         * If this function returns true, then the previous operation may be quietly undone and this operation replace
         * it. I.e. premises for the operation to succeed may have changed from construction time to the time point when
         * apply is called.
         * 
         * @param aOperation
         *            The {@link Command} to check with.
         * @return <code>true</code> if <code>this</code> can coalescele with aOperation.
         */
        public boolean canCoalescele(@SuppressWarnings("unused") Command aOperation) {
            return false;
        }

        /**
         * @return A {@link String} containing a (short) human readable description of this action.
         */
        public abstract String describe();

        @Override
        public String toString() {
            return describe();
        }

        /**
         * Will 'do' this operation
         * 
         * @throws Exception
         *             If the operation failed.
         */
        protected abstract void apply() throws Exception;

        /**
         * Will undo this action.
         * 
         */
        protected abstract void undo();
    }

    /**
     * This class models an operation that should be considered as one but actually consists of many smaller operations
     * that are all performed in order as one transaction.
     * 
     * @author Emily Björk
     */
    public abstract static class CompositeCommand extends Command {
        protected final MessageBuffer messageBuffer = new MessageBuffer();
        private final List<Command>   commands      = new ArrayList<>();
        private final String          desciption;
        private transient boolean     isPerpared    = false;
        private final MessageDelivery messageTarget;

        public CompositeCommand(String aDescription, MessageDelivery aMessageTarget) {
            desciption = aDescription;
            messageTarget = aMessageTarget;
        }

        public void addOp(Command anOperation) {
            commands.add(anOperation);
        }

        @Override
        public String describe() {
            return desciption;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof CompositeCommand))
                return false;
            CompositeCommand other = (CompositeCommand) obj;
            if (desciption == null) {
                if (other.desciption != null)
                    return false;
            }
            else if (!desciption.equals(other.desciption))
                return false;
            if (commands == null) {
                if (other.commands != null)
                    return false;
            }
            else if (!commands.equals(other.commands))
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((desciption == null) ? 0 : desciption.hashCode());
            result = prime * result + ((commands == null) ? 0 : commands.hashCode());
            return result;
        }

        public void prepareCommandAheadOfTime() throws EquipException {
            if (!isPerpared) {
                buildCommand();
                isPerpared = true;
            }
        }

        @Override
        protected void apply() throws Exception {
            if (!isPerpared) {
                buildCommand();
                isPerpared = true;
            }

            ListIterator<Command> it = commands.listIterator();
            while (it.hasNext()) {
                try {
                    it.next().apply();
                }
                catch (Throwable t) {
                    // Roll back the transaction
                    it.previous();
                    while (it.hasPrevious()) {
                        it.previous().undo();
                    }
                    throw t;
                }
            }

            messageBuffer.deliverTo(messageTarget);
        }

        /**
         * The user should implement this to create the operation. Will be called only once, immediately before the
         * first time the operation is applied.
         * 
         * @throws EquipException
         *             If for some reason the command failed to build.
         */
        protected abstract void buildCommand() throws EquipException;

        @Override
        protected void undo() {
            if (!isPerpared) {
                throw new IllegalStateException("Undo called before apply!");
            }

            // Do it in the "right" i.e. backwards order
            ListIterator<Command> it = commands.listIterator(commands.size());
            while (it.hasPrevious()) {
                it.previous().undo();
            }

            messageBuffer.deliverTo(messageTarget);
        }
    }

    private final List<Command>          actions      = new LinkedList<>();
    private int                          currentCmd   = -1;
    private final int                    depth;

    private final ObjectBinding<Command> nextRedoProp = new ObjectBinding<CommandStack.Command>() {
                                                          @Override
                                                          protected Command computeValue() {
                                                              return nextRedo();
                                                          }
                                                      };

    private final ObjectBinding<Command> nextUndoProp = new ObjectBinding<CommandStack.Command>() {
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
    public CommandStack(int aUndoDepth) {
        depth = aUndoDepth;
    }

    public Command nextRedo() {
        if (currentCmd + 1 >= actions.size())
            return null;

        return actions.get(currentCmd + 1);
    }

    public ObjectBinding<Command> nextRedoProperty() {
        return nextRedoProp;
    }

    public Command nextUndo() {
        if (currentCmd < 0)
            return null;
        return actions.get(currentCmd);
    }

    public ObjectBinding<Command> nextUndoProperty() {
        return nextUndoProp;
    }

    public void pushAndApply(Command aCmd) throws Exception {
        // Perform automatic coalescing
        int cmdBeforeCoalescele = currentCmd;
        while (nextUndo() != null && nextUndo().canCoalescele(aCmd)) {
            undo();
        }

        try {
            aCmd.apply();
        }
        catch (Exception throwable) {
            // Undo the coalescing if the new operation threw.
            while (currentCmd != cmdBeforeCoalescele && nextRedo() != null) {
                redo();
            }
            throw throwable;
        }
        while (currentCmd < actions.size() - 1) {
            // Previously undone actions in the list
            actions.remove(actions.size() - 1);
        }
        actions.add(aCmd);
        currentCmd = actions.size() - 1;

        while (actions.size() > depth) {
            actions.remove(0);
            currentCmd--;
        }
        // FIXME: Unit test the bindings functionality.
        updateBindings(); // FIXME: does this need to be in a try-catch on apply?
    }

    private void updateBindings() {
        nextRedoProp.invalidate();
        nextUndoProp.invalidate();
    }

    public void redo() throws Exception {
        Command cmd = nextRedo();
        if (null != cmd) {
            cmd.apply();
            currentCmd++;
        }
        updateBindings(); // FIXME: does this need to be in a try-catch on apply?
    }

    public void undo() {
        Command cmd = nextUndo();
        if (null != cmd) {
            cmd.undo();
            currentCmd--;
        }
        updateBindings(); // FIXME: does this need to be in a try-catch on undo?
    }
}
