/* $RCSfile$
 * $Author$
 * $Date$
 * $Revision$
 *
 * Copyright (C) 2001-2003  Jmol Project
 * Copyright (C) 2003-2004  The Chemistry Development Kit (CDK) project
 *
 * Contact: cdk-devel@lists.sourceforge.net
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307  USA.
 */
package org.openscience.cdk.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import org.openscience.cdk.Molecule;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.LoggingTool;

/**
 * A factory for creating ChemObjectReaders. The type of reader
 * created is determined from the content of the input. Formats
 * of GZiped files can be detected too.
 *
 * A typical example is:
 * <pre>
 *   StringReader stringReader = "&lt;molecule/>";
 *   ChemObjectReader reader = new ReaderFactory().createReader(stringReader);
 * </pre>
 *
 * @cdk.module io
 *
 * @author  Egon Willighagen <egonw@sci.kun.nl>
 * @author  Bradley A. Smith <bradley@baysmith.com>
 */
public class ReaderFactory {
    
    private int headerLength;
    private LoggingTool logger;

    private static Vector readers;

    /**
     * Constructs a ReaderFactory which tries to detect the format in the
     * first 65536 chars.
     */
    public ReaderFactory() {
        this(65536);
    }

    /**
     * Constructs a ReaderFactory which tries to detect the format in the
     * first given number of chars.
     *
     * @param headerLength length of the header in number of chars
     */
    public ReaderFactory(int headerLength) {
        logger = new LoggingTool(this);
        this.headerLength = headerLength;
        loadReaders();
    }

    /**
     * Registers a reader for format detection. To be useful, the
     * registered ChemObjectReader must implement the matches()
     * method.
     *
     * @see org.openscience.cdk.io.ChemObjectReader#matches(int,String)
     */
    public void registerReader(ChemObjectReader reader) {
        readers.addElement(reader);
    }

    private void loadReaders() {
        // IMPORTANT: the order in the next series *is* important!
        String[] readerNames = {
            "org.openscience.cdk.io.ABINITReader.java",
            "org.openscience.cdk.io.Aces2Reader.java",
            "org.openscience.cdk.io.ADFReader.java",
            "org.openscience.cdk.io.CACheReader.java",
            "org.openscience.cdk.io.ChemicalRSSReader.java",
            "org.openscience.cdk.io.CIFReader.java",
            "org.openscience.cdk.io.CMLReader.java",
            "org.openscience.cdk.io.CrystClustReader.java",
            "org.openscience.cdk.io.DaltonReader.java",
            "org.openscience.cdk.io.GamessReader.java",
            "org.openscience.cdk.io.Gaussian03Reader.java",
            "org.openscience.cdk.io.Gaussian98Reader.java",
            "org.openscience.cdk.io.Gaussian95Reader.java",
            "org.openscience.cdk.io.Gaussian94Reader.java",
            "org.openscience.cdk.io.Gaussian92Reader.java",
            "org.openscience.cdk.io.Gaussian90Reader.java",
            "org.openscience.cdk.io.GhemicalMMReader.java",
            "org.openscience.cdk.io.HINReader.java",
            "org.openscience.cdk.io.IChIReader.java",
            "org.openscience.cdk.io.JaguarReader.java",
            "org.openscience.cdk.io.MACiEReader.java",
            "org.openscience.cdk.io.MDLReader.java",
            "org.openscience.cdk.io.MDLRXNV3000Reader.java",
            "org.openscience.cdk.io.MDLRXNReader.java",
            "org.openscience.cdk.io.MDLV3000Reader.java",
            "org.openscience.cdk.io.Mol2Reader.java",
            "org.openscience.cdk.io.MOPAC7Reader.java",
            "org.openscience.cdk.io.MOPAC97Reader.java",
            "org.openscience.cdk.io.PDBReader.java",
            "org.openscience.cdk.io.PMPReader.java",
            "org.openscience.cdk.io.ShelXReader.java",
            "org.openscience.cdk.io.SMILESReader.java",
            "org.openscience.cdk.io.VASPReader.java",
            "org.openscience.cdk.io.XYZReader.java",
            "org.openscience.cdk.io.ZMatrixReader.java"
        };
        readers = new Vector();
        for (int i=0; i<readerNames.length; i++) {
            // load them one by one
            try {
                ChemObjectReader coReader = (ChemObjectReader)this.getClass().getClassLoader().
                    loadClass(readerNames[i]).newInstance();
            } catch (ClassNotFoundException exception) {
                logger.error("Could not find this ChemObjectReader: ", readerNames[i]);
                logger.debug(exception);
            } catch (Exception exception) {
                logger.error("Could not load this ChemObjectReader: ", readerNames[i]);
                logger.debug(exception);
            }
        }
    }

    /**
     * Creates a String of the Class name of the ChemObject reader
     * for this file format. The input is read line-by-line
     * until a line containing an identifying string is
     * found.
     *
     * <p>The ReaderFactory detects more formats than the CDK
     * has Readers for. If the CDK IO Reader is an instance of 
     * DummyReader, than it Reader is not implemented.
     *
     * <p>This method is not able to detect the format of gziped files.
     * Use guessFormat(InputStream) instead for such files.
     *
     * @throws IOException  if an I/O error occurs
     * @throws IllegalArgumentException if the input is null
     *
     * @see #guessFormat(InputStream)
     */
    public String guessFormat(Reader input) throws IOException {
        ChemObjectReader reader = createReader(input);
        if (reader != null) {
            return reader.getClass().getName();
        }
        return "Format undetermined";
    }
    
    public String guessFormat(InputStream input) throws IOException {
        ChemObjectReader reader = createReader(input);
        if (reader != null) {
            return reader.getClass().getName();
        }
        return "Format undetermined";
    }
    
    /**
     * Detects the format of the Reader input, and if known, it will return
     * a CDK Reader to read the format. Note that this Reader might be a
     * subclass of DummyReader, which means that the Reader does not yet 
     * have an implementation.
     *
     * @see #createReader(Reader)
     * @see org.openscience.cdk.io.DummyReader
     */
    public ChemObjectReader createReader(InputStream input) throws IOException {
        BufferedInputStream bistream = new BufferedInputStream(input, 8192);
        InputStream istreamToRead = bistream; // if gzip test fails, then take default
        bistream.mark(5);
        int countRead = 0;
        try {
            byte[] abMagic = new byte[4];
            countRead = bistream.read(abMagic, 0, 4);
            bistream.reset();
            if (countRead == 4) {
                if (abMagic[0] == (byte)0x1F && abMagic[1] == (byte)0x8B) {
                    istreamToRead = new GZIPInputStream(bistream);
                }
            }
        } catch (IOException exception) {
            logger.error(exception.getMessage());
            logger.debug(exception);
        }
        return createReader(new InputStreamReader(istreamToRead));
    }
    
    /**
     * Detects the format of the Reader input, and if known, it will return
     * a CDK Reader to read the format. Note that this Reader might be a
     * subclass of DummyReader, which means that the Reader does not yet 
     * have an implementation.
     *
     * <p>This method is not able to detect the format of gziped files.
     * Use createReader(InputStream) instead for such files.
     *
     * @see #createReader(InputStream)
     * @see org.openscience.cdk.io.DummyReader
     */
    public ChemObjectReader createReader(Reader input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("input cannot be null");
        }

        // FIXME: this should use the new ChemObjectReader.matches() method

        // make a copy of the header
        int bufferSize = this.headerLength;
        BufferedReader originalBuffer = new BufferedReader(input, bufferSize);
        char[] header = new char[bufferSize];
        if (!originalBuffer.markSupported()) {
            logger.error("Mark not supported");
            throw new IllegalArgumentException("input must support mark");
        }
        originalBuffer.mark(bufferSize);
        originalBuffer.read(header, 0, bufferSize);
        originalBuffer.reset();
        
        BufferedReader buffer = new BufferedReader(new CharArrayReader(header));
        
        /* Search file for a line containing an identifying keyword */
        String line = buffer.readLine();
        int lineNumber = 1;
        while (buffer.ready() && (line != null)) {
            logger.debug(lineNumber + ": ", line);
            Enumeration readerEnum = readers.elements();
            while (readerEnum.hasMoreElements()) {
                ChemObjectReader coReader = (ChemObjectReader)readerEnum.nextElement();
                if (coReader.matches(lineNumber, line)) {
                    try {
                        // make a new instance of this class
                        ChemObjectReader returnReader = (ChemObjectReader)coReader.getClass().newInstance();
                        returnReader.setReader(originalBuffer);
                        logger.info("Detected format: ", returnReader.getFormatName());
                        return returnReader;
                    } catch (Exception exception) {
                        logger.error("Could not create this ChemObjectReader: ", coReader.getClass().getName());
                        logger.debug(exception);
                    }
                }
            }
            line = buffer.readLine();
            lineNumber++;
        }

        logger.warn("Now comes the tricky and more difficult ones....");
        buffer = new BufferedReader(new CharArrayReader(header));
        
        line = buffer.readLine();
        // is it a XYZ file?
        StringTokenizer tokenizer = new StringTokenizer(line.trim());
        try {
            int tokenCount = tokenizer.countTokens();
            if (tokenCount == 1) {
                new Integer(tokenizer.nextToken());
                // if not failed, then it is a XYZ file
                return new org.openscience.cdk.io.XYZReader(originalBuffer);
            } else if (tokenCount == 2) {
                new Integer(tokenizer.nextToken());
                if ("Bohr".equalsIgnoreCase(tokenizer.nextToken())) {
                    return new org.openscience.cdk.io.XYZReader(originalBuffer);
                }
            }
        } catch (NumberFormatException exception) {
            logger.info("No, it's not a XYZ file");
        }
        // is it a SMILES file?
        try {
            SmilesParser sp = new SmilesParser();
            Molecule m = sp.parseSmiles(line);
            return new org.openscience.cdk.io.SMILESReader(originalBuffer);
        } catch (InvalidSmilesException ise) {
            // no, it is not
            logger.info("No, it's not a SMILES file");
        }

        logger.warn("File format undetermined");
        return null;
    }

}
