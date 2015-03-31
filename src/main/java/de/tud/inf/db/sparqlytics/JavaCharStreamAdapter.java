// SPARQLytics: Multidimensional Analytics for RDF Data.
// Copyright (C) 2015  Michael Rudolf
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package de.tud.inf.db.sparqlytics;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

/**
 * Adapter for SPARQLytics Java character streams, so that they can be used by
 * the Jena SPARQL parser.
 *
 * @author Michael Rudolf
 */
public class JavaCharStreamAdapter 
        extends com.hp.hpl.jena.sparql.lang.sparql_11.JavaCharStream {
    /**
     * The Java character stream to adapt.
     */
    private final JavaCharStream source;
    
    /**
     * Creates a new adapter for the given Java character stream.
     * 
     * @param source the Java character stream to adapt
     */
    public JavaCharStreamAdapter(JavaCharStream source) {
        super((Reader)null);
        this.source = source;
    }

    @Override
    protected void AdjustBuffSize() {
        source.AdjustBuffSize();
    }

    @Override
    public char BeginToken() throws IOException {
        return source.BeginToken();
    }

    @Override
    public void Done() {
        source.Done();
    }

    @Override
    protected void ExpandBuff(boolean wrapAround) {
        source.ExpandBuff(wrapAround);
    }

    @Override
    protected void FillBuff() throws IOException {
        source.FillBuff();
    }

    @Override
    public String GetImage() {
        return source.GetImage();
    }

    @Override
    public char[] GetSuffix(int len) {
        return source.GetSuffix(len);
    }

    @Override
    public void ReInit(InputStream dstream) {
        source.ReInit(dstream);
    }

    @Override
    public void ReInit(Reader dstream) {
        source.ReInit(dstream);
    }

    @Override
    public void ReInit(InputStream dstream, String encoding) 
            throws UnsupportedEncodingException {
        source.ReInit(dstream, encoding);
    }

    @Override
    public void ReInit(InputStream dstream, int startline, int startcolumn) {
        source.ReInit(dstream, startline, startcolumn);
    }

    @Override
    public void ReInit(Reader dstream, int startline, int startcolumn) {
        source.ReInit(dstream, startline, startcolumn);
    }

    @Override
    public void ReInit(InputStream dstream, String encoding, int startline, int startcolumn) 
            throws UnsupportedEncodingException {
        source.ReInit(dstream, encoding, startline, startcolumn);
    }

    @Override
    public void ReInit(InputStream dstream, int startline, int startcolumn, int buffersize) {
        source.ReInit(dstream, startline, startcolumn, buffersize);
    }

    @Override
    public void ReInit(Reader dstream, int startline, int startcolumn, int buffersize) {
        source.ReInit(dstream, startline, startcolumn, buffersize);
    }

    @Override
    public void ReInit(InputStream dstream, String encoding, int startline, int startcolumn, int buffersize) 
            throws UnsupportedEncodingException {
        source.ReInit(dstream, encoding, startline, startcolumn, buffersize);
    }

    @Override
    protected char ReadByte() throws IOException {
        return source.ReadByte();
    }

    @Override
    protected void UpdateLineColumn(char c) {
        source.UpdateLineColumn(c);
    }

    @Override
    public void adjustBeginLineColumn(int newLine, int newCol) {
        source.adjustBeginLineColumn(newLine, newCol);
    }

    @Override
    public void backup(int amount) {
        source.backup(amount);
    }

    @Override
    public int getBeginColumn() {
        return source.getBeginColumn();
    }

    @Override
    public int getBeginLine() {
        return source.getBeginLine();
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getColumn() {
        return source.getColumn();
    }

    @Override
    public int getEndColumn() {
        return source.getEndColumn();
    }

    @Override
    public int getEndLine() {
        return source.getEndLine();
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getLine() {
        return source.getLine();
    }

    @Override
    protected int getTabSize(int i) {
        return source.getTabSize(i);
    }

    @Override
    public char readChar() throws IOException {
        return source.readChar();
    }

    @Override
    protected void setTabSize(int i) {
        source.setTabSize(i);
    }
}
