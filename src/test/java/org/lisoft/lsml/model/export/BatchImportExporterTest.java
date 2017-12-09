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
package org.lisoft.lsml.model.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.model.garage.GarageDirectory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.DecodingException;
import org.lisoft.lsml.util.EncodingException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

/**
 * This is the test suite for {@link BatchImportExporter} class.
 *
 * @author Li Song
 */
public class BatchImportExporterTest {
    private final ErrorReporter errorReporting = mock(ErrorReporter.class);
    private final Base64LoadoutCoder coder = mock(Base64LoadoutCoder.class);
    private final Loadout loadout1 = mock(Loadout.class);
    private final Loadout loadout2 = mock(Loadout.class);
    private final Loadout loadout3 = mock(Loadout.class);
    private final Loadout loadout4 = mock(Loadout.class);
    private final Loadout loadout5 = mock(Loadout.class);
    private final Loadout loadout6 = mock(Loadout.class);
    private final String name1 = "name1";
    private final String name2 = "name2";
    private final String name3 = "name3";
    private final String name4 = "name4";
    private final String name5 = "name5";
    private final String name6 = "name6";
    private final String code1 = "lsml://abc";
    private final String code2 = "lsml://def";
    private final String code3 = "lsml://ghi";
    private final String code4 = "lsml://jkl";
    private final String code5 = "lsml://mnopq";
    private final String code6 = "lsml://rstuvxyz";
    private final String code1http = "http://t.li-soft.org/?l=rgATKDcFMhJNBzcFMCjne6%2FupzrMmOtmOq23MJbMdVtmOtk%3D";
    private final String code2http = "http://t.li-soft.org/?l=rgATKDcFOhJNBzcFOCjne6%2FupzrMY7lZjWy9L126LmFL0vXKzGtl6Xqx2Q%3D%3D";
    private final String code3http = "http://ghi";
    private final String code4http = "http://jkl";
    private final String code5http = "http://mnopq";
    private final String code6http = "http://rstuvxyz";
    private final BatchImportExporter cut = new BatchImportExporter(coder, errorReporting);

    // TODO: Write tests for broken inputs and fix implementation. Currently it
    // assumes reasonably well-formed input.

    @Captor
    ArgumentCaptor<Throwable> errorArguments;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Before
    public void setup() throws Exception {
        when(coder.encodeLSML(loadout1)).thenReturn(code1);
        when(coder.encodeLSML(loadout2)).thenReturn(code2);
        when(coder.encodeLSML(loadout3)).thenReturn(code3);
        when(coder.encodeLSML(loadout4)).thenReturn(code4);
        when(coder.encodeLSML(loadout5)).thenReturn(code5);
        when(coder.encodeLSML(loadout6)).thenReturn(code6);
        when(coder.encodeHTTPTrampoline(loadout1)).thenReturn(code1http);
        when(coder.encodeHTTPTrampoline(loadout2)).thenReturn(code2http);
        when(coder.encodeHTTPTrampoline(loadout3)).thenReturn(code3http);
        when(coder.encodeHTTPTrampoline(loadout4)).thenReturn(code4http);
        when(coder.encodeHTTPTrampoline(loadout5)).thenReturn(code5http);
        when(coder.encodeHTTPTrampoline(loadout6)).thenReturn(code6http);
        when(coder.parse(code1)).thenReturn(loadout1);
        when(coder.parse(code1http)).thenReturn(loadout1);
        when(coder.parse(code2)).thenReturn(loadout2);
        when(coder.parse(code2http)).thenReturn(loadout2);
        when(coder.parse(code3)).thenReturn(loadout3);
        when(coder.parse(code3http)).thenReturn(loadout3);
        when(coder.parse(code4)).thenReturn(loadout4);
        when(coder.parse(code4http)).thenReturn(loadout4);
        when(coder.parse(code5)).thenReturn(loadout5);
        when(coder.parse(code5http)).thenReturn(loadout5);
        when(coder.parse(code6)).thenReturn(loadout6);
        when(coder.parse(code6http)).thenReturn(loadout6);
        when(loadout1.getName()).thenReturn(name1);
        when(loadout2.getName()).thenReturn(name2);
        when(loadout3.getName()).thenReturn(name3);
        when(loadout4.getName()).thenReturn(name4);
        when(loadout5.getName()).thenReturn(name5);
        when(loadout6.getName()).thenReturn(name6);
    }

    @Test
    public void testExport_EmptyDirectories() throws EncodingException {
        final String rootName = "root";
        final String sub1Name = "sub1";
        final String sub1sub1Name = "sub1sub1";

        final GarageDirectory<Loadout> root = mock(GarageDirectory.class);
        final GarageDirectory<Loadout> sub1 = mock(GarageDirectory.class);
        final GarageDirectory<Loadout> sub1sub1 = mock(GarageDirectory.class);

        setupDir(rootName, root, Arrays.asList(sub1), Collections.EMPTY_LIST);
        setupDir(sub1Name, sub1, Arrays.asList(sub1sub1), Collections.EMPTY_LIST);
        setupDir(sub1sub1Name, sub1sub1, Collections.EMPTY_LIST, Arrays.asList(loadout1));

        final String result = cut.export(root);

        String expected = "";
        expected += '[' + rootName + '/' + sub1Name + '/' + sub1sub1Name + ']' + "\n";
        expected += "{" + name1 + "} " + code1 + "\n";
        assertEquals(expected, result);
    }

    @Test
    public void testExport_ManyLoadoutsHTTP() throws EncodingException {
        final GarageDirectory<Loadout> root = mock(GarageDirectory.class);
        final String dirName = "directory";
        setupDir(dirName, root, Collections.EMPTY_LIST, Arrays.asList(loadout1, loadout2, loadout3));

        cut.setProtocol(LsmlLinkProtocol.HTTP);
        final String result = cut.export(root);

        String expected = "[" + dirName + "]" + "\n";
        expected += "{" + name1 + "} " + code1http + "\n";
        expected += "{" + name2 + "} " + code2http + "\n";
        expected += "{" + name3 + "} " + code3http + "\n";
        assertEquals(expected, result);
    }

    @Test
    public void testExport_ManyLoadoutsLSML() throws EncodingException {
        final GarageDirectory<Loadout> root = mock(GarageDirectory.class);
        final String dirName = "directory";
        setupDir(dirName, root, Collections.EMPTY_LIST, Arrays.asList(loadout1, loadout2, loadout3));

        final String result = cut.export(root);

        String expected = "[" + dirName + "]" + "\n";
        expected += "{" + name1 + "} " + code1 + "\n";
        expected += "{" + name2 + "} " + code2 + "\n";
        expected += "{" + name3 + "} " + code3 + "\n";
        assertEquals(expected, result);
    }

    @Test
    public void testExport_NestedDirectories() throws EncodingException {
        final String rootName = "root";
        final String sub1Name = "sub1";
        final String sub2Name = "sub2";
        final String sub1sub1Name = "sub1sub1";
        final String sub1sub2Name = "sub1sub2";

        final GarageDirectory<Loadout> root = mock(GarageDirectory.class);
        final GarageDirectory<Loadout> sub1 = mock(GarageDirectory.class);
        final GarageDirectory<Loadout> sub2 = mock(GarageDirectory.class);
        final GarageDirectory<Loadout> sub1sub1 = mock(GarageDirectory.class);
        final GarageDirectory<Loadout> sub1sub2 = mock(GarageDirectory.class);

        setupDir(rootName, root, Arrays.asList(sub1, sub2), Arrays.asList(loadout1));
        setupDir(sub1Name, sub1, Arrays.asList(sub1sub1, sub1sub2), Arrays.asList(loadout2));
        setupDir(sub2Name, sub2, Collections.EMPTY_LIST, Arrays.asList(loadout3));
        setupDir(sub1sub1Name, sub1sub1, Collections.EMPTY_LIST, Arrays.asList(loadout4, loadout5));
        setupDir(sub1sub2Name, sub1sub2, Collections.EMPTY_LIST, Arrays.asList(loadout6));

        final String result = cut.export(root);

        String expected = '[' + rootName + ']' + "\n";
        expected += "{" + name1 + "} " + code1 + "\n";

        expected += '[' + rootName + '/' + sub1Name + ']' + "\n";
        expected += "{" + name2 + "} " + code2 + "\n";

        expected += '[' + rootName + '/' + sub1Name + '/' + sub1sub1Name + ']' + "\n";
        expected += "{" + name4 + "} " + code4 + "\n";
        expected += "{" + name5 + "} " + code5 + "\n";

        expected += '[' + rootName + '/' + sub1Name + '/' + sub1sub2Name + ']' + "\n";
        expected += "{" + name6 + "} " + code6 + "\n";

        expected += '[' + rootName + '/' + sub2Name + ']' + "\n";
        expected += "{" + name3 + "} " + code3 + "\n";
        assertEquals(expected, result);
    }

    @Test
    public void testImport_BadFormatReportedSpec() throws Exception {
        final String badLoadout = "lsml://asdasd";
        String data = "[foobar]" + "\n";
        final String rubish = "{rubish name " + badLoadout;
        data += rubish + "\n";

        final DecodingException error = mock(DecodingException.class);
        when(coder.parse(badLoadout)).thenThrow(error);

        cut.parse(data);

        verify(coder, never()).parse(badLoadout);
        verify(errorReporting).error(eq("Parse error"), eq("Unable to parse line: " + rubish),
                errorArguments.capture());

        final List<Throwable> errors = errorArguments.getAllValues();
        assertEquals(1, errors.size());
        assertTrue(errors.get(0) instanceof IOException);
    }

    @Test
    public void testImport_BrokenLinkReportedSpec() throws Exception {
        final String badLoadout = "lsml://b0rken";
        String data = "[foobar]" + "\n";
        final String rubbishData = "{rubish name }" + badLoadout;
        data += rubbishData + "\n";

        final DecodingException error = mock(DecodingException.class);
        when(coder.parse(badLoadout)).thenThrow(error);

        cut.parse(data);

        verify(errorReporting).error("Parse error", "Unable to parse line: " + rubbishData, error);
    }

    @Test
    public void testImport_DeepFolders() {
        final String rootName = "rootX";
        final String sub1Name = "sub1";
        final String sub2Name = "sub2";
        final String sub1sub1Name = "sub1sub1";

        String data = "[" + rootName + "]" + "\n";
        data += "{ " + name1 + "} " + code1 + "\n";
        data += "[" + rootName + "/" + sub1Name + "/" + sub1sub1Name + "]" + "\n";
        data += "{" + name2 + " }" + code2http + " \n";
        data += "[" + rootName + "/" + sub2Name + "]" + "\n";
        data += "{" + name3 + "} " + code3 + "\n";
        data += "[" + rootName + "/" + sub1Name + "]" + "\n";
        data += "{" + name4 + "} " + code4http + "\n";

        final GarageDirectory<Loadout> implicitRoot = cut.parse(data);

        assertEquals("", implicitRoot.getName());
        assertEquals(1, implicitRoot.getDirectories().size());
        assertEquals(0, implicitRoot.getValues().size());

        final GarageDirectory<Loadout> root = implicitRoot.getDirectories().get(0);
        assertEquals(rootName, root.getName());
        assertEquals(1, root.getValues().size());
        assertSame(loadout1, root.getValues().get(0));

        assertEquals(2, root.getDirectories().size());
        final GarageDirectory<Loadout> sub1 = root.getDirectories().get(0);
        assertEquals(sub1Name, sub1.getName());
        assertEquals(1, sub1.getValues().size());
        assertSame(loadout4, sub1.getValues().get(0));

        final GarageDirectory<Loadout> sub2 = root.getDirectories().get(1);
        assertEquals(sub2Name, sub2.getName());
        assertEquals(1, sub2.getValues().size());
        assertSame(loadout3, sub2.getValues().get(0));

        assertEquals(1, sub1.getDirectories().size());
        final GarageDirectory<Loadout> sub1sub1 = sub1.getDirectories().get(0);
        assertEquals(sub1sub1Name, sub1sub1.getName());
        assertEquals(1, sub1sub1.getValues().size());
        assertSame(loadout2, sub1sub1.getValues().get(0));

        verify(loadout1).setName(name1);
        verify(loadout2).setName(name2);
        verify(loadout3).setName(name3);
        verify(loadout4).setName(name4);
    }

    @Test
    public void testImport_ImplicitRoot() {
        String data = "";
        data += "{" + name1 + "} " + code1 + "\n";
        data += "{" + name2 + "} " + code2http + "\n";
        data += "{" + name4 + "} " + code4 + "\n";

        final GarageDirectory<Loadout> root = cut.parse(data);

        assertEquals("", root.getName());
        assertEquals(3, root.getValues().size());
        assertSame(loadout1, root.getValues().get(0));
        assertSame(loadout2, root.getValues().get(1));
        assertSame(loadout4, root.getValues().get(2));
        verify(loadout1).setName(name1);
        verify(loadout2).setName(name2);
        verify(loadout4).setName(name4);
    }

    @Test
    public void testImport_ManyLoadouts() {
        final String rootName = "rootX";
        String data = "[" + rootName + "]" + "\n";
        data += "{" + name1 + "} " + code1 + "\n";
        data += "{" + name2 + "} " + code2http + "\n";
        data += "{" + name4 + "} " + code4 + "\n";

        final GarageDirectory<Loadout> implicitRoot = cut.parse(data);
        assertEquals(1, implicitRoot.getDirectories().size());
        final GarageDirectory<Loadout> root = implicitRoot.getDirectories().get(0);
        assertEquals(rootName, root.getName());
        assertEquals(3, root.getValues().size());
        assertSame(loadout1, root.getValues().get(0));
        assertSame(loadout2, root.getValues().get(1));
        assertSame(loadout4, root.getValues().get(2));
        verify(loadout1).setName(name1);
        verify(loadout2).setName(name2);
        verify(loadout4).setName(name4);
    }

    @Test
    public void testImportExport() throws EncodingException {
        final GarageDirectory<Loadout> root = new GarageDirectory<>("");
        final GarageDirectory<Loadout> sub1 = new GarageDirectory<>("sub1");
        final GarageDirectory<Loadout> sub1sub1 = new GarageDirectory<>("sub1sub1");

        root.getValues().add(loadout1);
        root.getValues().add(loadout2);
        sub1.getValues().add(loadout3);
        sub1.getValues().add(loadout4);
        sub1sub1.getValues().add(loadout5);
        sub1sub1.getValues().add(loadout6);
        root.getDirectories().add(sub1);
        sub1.getDirectories().add(sub1sub1);

        final GarageDirectory<Loadout> parsedRoot = cut.parse(cut.export(root));

        assertEquals("", parsedRoot.getName());
        assertEquals(1, parsedRoot.getDirectories().size());
        assertEquals(2, parsedRoot.getValues().size());
        assertSame(loadout1, parsedRoot.getValues().get(0));
        assertSame(loadout2, parsedRoot.getValues().get(1));

        final GarageDirectory<Loadout> parsedSub1 = parsedRoot.getDirectories().get(0);
        assertEquals(1, parsedSub1.getDirectories().size());
        assertEquals(2, parsedSub1.getValues().size());
        assertSame(loadout3, parsedSub1.getValues().get(0));
        assertSame(loadout4, parsedSub1.getValues().get(1));

        final GarageDirectory<Loadout> parsedSub1sub1 = parsedSub1.getDirectories().get(0);
        assertEquals(0, parsedSub1sub1.getDirectories().size());
        assertEquals(2, parsedSub1sub1.getValues().size());
        assertSame(loadout5, parsedSub1sub1.getValues().get(0));
        assertSame(loadout6, parsedSub1sub1.getValues().get(1));
    }

    private void setupDir(String aName, GarageDirectory<Loadout> aDir, List<GarageDirectory<Loadout>> aChildren,
            List<Loadout> aLoadouts) {
        when(aDir.getDirectories()).thenReturn(Collections.unmodifiableList(aChildren));
        when(aDir.getValues()).thenReturn(aLoadouts);
        when(aDir.getName()).thenReturn(aName);
    }
}
