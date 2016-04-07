/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2012, Telestax Inc and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.protocols.ss7.isup.impl.message.parameter;

import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;
import org.mobicents.protocols.ss7.isup.ParameterException;
import org.mobicents.protocols.ss7.isup.message.parameter.GenericDigits;
import org.mobicents.protocols.ss7.isup.util.BcdHelper;

import javax.xml.bind.DatatypeConverter;

/**
 * Start time:12:24:47 2009-03-31<br>
 * Project: mobicents-isup-stack<br>
 *
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 * @author <a href="mailto:grzegorz.figiel@pro-ids.com"> Grzegorz Figiel </a>
 */
public class GenericDigitsImpl extends AbstractISUPParameter implements GenericDigits {

    private static final String ENCODING_SCHEME = "encodingScheme";
    private static final String TYPE_OF_DIGITS = "typeOfDigits";
    private static final String DIGITS = "digits";

    private static final int DEFAULT_VALUE = 0;

    private int encodingScheme;
    private int typeOfDigits;
    private byte[] digits;

    public GenericDigitsImpl(byte[] b) throws ParameterException {
        super();
        decode(b);
    }

    public GenericDigitsImpl(int encodingScheme, int typeOfDigits, byte[] digits) {
        super();
        this.encodingScheme = encodingScheme;
        this.typeOfDigits = typeOfDigits;
        this.setEncodedDigits(digits);
    }

    public GenericDigitsImpl(int encodingScheme, int typeOfDigits, String digits) {
        super();
        this.encodingScheme = encodingScheme;
        this.typeOfDigits = typeOfDigits;
        this.setEncodedDigits(BcdHelper.stringToBCD(digits));
    }

    public GenericDigitsImpl() {
        super();

    }

    public String getDecodedDigits() {
        boolean isOdd = this.encodingScheme == GenericDigits._ENCODING_SCHEME_BCD_ODD ? true : false;
        String result = BcdHelper.bcdToString(isOdd, digits);
        return result;
    }

    public void setDecodedDigits(int encodingScheme, String digits) {
        if (digits == null || digits.length() < 1) {
            throw new IllegalArgumentException("Digits must not be null");
        }
        //TODO: analyse if encoding scheme is correctly set (ODD/EVEN) vs number of digits
        this.encodingScheme = encodingScheme;
        this.setEncodedDigits(BcdHelper.stringToBCD(digits));
    }

    public int decode(byte[] b) throws ParameterException {
        if (b == null || b.length < 2) {
            throw new ParameterException("byte[] must not be null or has size less than 2");
        }
        this.typeOfDigits = b[0] & 0x1F;
        this.encodingScheme = (b[0] >> 5) & 0x07;
        this.digits = new byte[b.length - 1];

        for (int index = 1; index < b.length; index++) {
            this.digits[index - 1] = b[index];
        }
        return 1 + this.digits.length;
    }

    public byte[] encode() throws ParameterException {

        byte[] b = new byte[this.digits.length + 1];

        b[0] |= this.typeOfDigits & 0x1F;
        b[0] |= ((this.encodingScheme & 0x07) << 5);

        for (int index = 1; index < b.length; index++) {
            b[index] = (byte) this.digits[index - 1];
        }
        return b;

    }

    public int getEncodingScheme() {
        return encodingScheme;
    }

    public void setEncodingScheme(int encodingScheme) {
        this.encodingScheme = encodingScheme;
    }

    public int getTypeOfDigits() {
        return typeOfDigits;
    }

    public void setTypeOfDigits(int typeOfDigits) {
        this.typeOfDigits = typeOfDigits;
    }

    public byte[] getEncodedDigits() {
        return digits;
    }

    public void setEncodedDigits(byte[] digits) {
        if (digits == null)
            throw new IllegalArgumentException("Digits must not be null");
        this.digits = digits;
    }

    public int getCode() {

        return _PARAMETER_CODE;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("GenericDigits [encodingScheme=");
        sb.append(encodingScheme);
        sb.append(", typeOfDigits=");
        sb.append(typeOfDigits);
        if (digits != null) {
            sb.append(", encodedDigits=[");
            sb.append(DatatypeConverter.printHexBinary(digits));
            sb.append("]");
        }
        sb.append("]");

        return sb.toString();
    }

    /**
     * XML Serialization/Deserialization
     */
    protected static final XMLFormat<GenericDigitsImpl> ISUP_GENERIC_DIGITS_XML = new XMLFormat<GenericDigitsImpl>(
            GenericDigitsImpl.class) {

        @Override
        public void read(javolution.xml.XMLFormat.InputElement xml, GenericDigitsImpl genericDigits) throws XMLStreamException {
            genericDigits.encodingScheme = xml.getAttribute(ENCODING_SCHEME, DEFAULT_VALUE);
            genericDigits.typeOfDigits = xml.getAttribute(TYPE_OF_DIGITS, DEFAULT_VALUE);

            ByteArrayContainer bc = xml.get(DIGITS, ByteArrayContainer.class);
            if (bc != null) {
                genericDigits.digits = bc.getData();
            }
        }

        @Override
        public void write(GenericDigitsImpl genericDigits, javolution.xml.XMLFormat.OutputElement xml)
                throws XMLStreamException {
            xml.setAttribute(ENCODING_SCHEME, genericDigits.encodingScheme);
            xml.setAttribute(TYPE_OF_DIGITS, genericDigits.typeOfDigits);

            if (genericDigits.digits != null) {
                ByteArrayContainer bac = new ByteArrayContainer(genericDigits.digits);
                xml.add(bac, DIGITS, ByteArrayContainer.class);
            }
        }
    };
}
