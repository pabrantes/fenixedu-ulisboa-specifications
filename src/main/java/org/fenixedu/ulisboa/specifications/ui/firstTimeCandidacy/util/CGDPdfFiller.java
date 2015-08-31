/**
 * Copyright © 2014 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Identification Cards.
 *
 * FenixEdu Identification Cards is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Identification Cards is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Identification Cards.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * https://github.com/ist-dsi/fenixedu-id-cards/blob/master/src/main/java/org/fenixedu/idcards/ui/candidacydocfiller/CGDPdfFiller.java
 */
package org.fenixedu.ulisboa.specifications.ui.firstTimeCandidacy.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.fenixedu.academic.domain.Person;
import org.joda.time.YearMonthDay;
import org.joda.time.format.DateTimeFormat;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

public class CGDPdfFiller {

    private static final String MARITAL_STATUS_CIVIL_UNION = "União de facto";
    private static final String MARITAL_STATUS_DIVORCED = "Divorciado";
    private static final String MARITAL_STATUS_SEPARATED = "Sep.judicialmente";
    private static final String MARITAL_STATUS_SINGLE = "Solteiro";
    private static final String MARITAL_STATUS_WIDOWER = "Víuvo";

    /*
     * PdfFiller variables and methods
     * Can not extend PdfFiller, since this class doesn't belong in the first candidacy report
     * */
    private AcroFields form;

    private String getMail(Person person) {
        if (person.hasInstitutionalEmailAddress()) {
            return person.getInstitutionalEmailAddressValue();
        } else {
            String emailForSendingEmails = person.getEmailForSendingEmails();
            return emailForSendingEmails != null ? emailForSendingEmails : StringUtils.EMPTY;
        }
    }

    private void setField(String fieldName, String fieldContent) throws IOException, DocumentException {
        if (fieldContent != null) {
            form.setField(fieldName, fieldContent);
        }
    }

    /*
     * End PdfFiller variables and methods
     * */

    public ByteArrayOutputStream getFilledPdf(Person person, InputStream pdfTemplateStream) throws IOException, DocumentException {
        return getFilledPdfCGDPersonalInformation(person, pdfTemplateStream);
    }

    private ByteArrayOutputStream getFilledPdfCGDPersonalInformation(Person person, InputStream pdfTemplateStream)
            throws IOException, DocumentException {
        PdfReader reader = new PdfReader(pdfTemplateStream);
        reader.getAcroForm().remove(PdfName.SIGFLAGS);
        reader.selectPages("1");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, output);
        form = stamper.getAcroFields();

        setField("T_NomeComp", person.getName());
        setField("T_Email", getMail(person));

        if (person.isFemale()) {
            setField("CB_0_1", "Yes"); // female
        } else {
            setField("CB_0_0", "Yes"); // male
        }

        setField("Cod_data_1", person.getDateOfBirthYearMonthDay().toString(DateTimeFormat.forPattern("yyyy/MM/dd")));

        setField("NIF1", person.getSocialSecurityNumber());
        setField("T_DocIdent", person.getDocumentIdNumber());

        switch (person.getMaritalStatus()) {
        case CIVIL_UNION:
            setField("CB_EstCivil01", MARITAL_STATUS_CIVIL_UNION);
            break;
        case DIVORCED:
            setField("CB_EstCivil01", MARITAL_STATUS_DIVORCED);
            break;
        case MARRIED:
            setField("CB_EstCivil01", "");
            break;
        case SEPARATED:
            setField("CB_EstCivil01", MARITAL_STATUS_SEPARATED);
            break;
        case SINGLE:
            setField("CB_EstCivil01", MARITAL_STATUS_SINGLE);
            break;
        case WIDOWER:
            setField("CB_EstCivil01", MARITAL_STATUS_WIDOWER);
            break;
        }
        YearMonthDay emissionDate = person.getEmissionDateOfDocumentIdYearMonthDay();
        if (emissionDate != null) {
            setField("Cod_data_2", emissionDate.toString(DateTimeFormat.forPattern("yyyy/MM/dd")));
        }

        YearMonthDay expirationDate = person.getExpirationDateOfDocumentIdYearMonthDay();
        if (expirationDate != null) {
            setField("Cod_data_3", expirationDate.toString(DateTimeFormat.forPattern("yyyy/MM/dd")));
        }

        setField("T_NomePai", person.getNameOfFather());
        setField("T_NomeMae", person.getNameOfMother());

        setField("T_NatPais", person.getCountryOfBirth().getName());
        setField("T_Naturali", person.getDistrictOfBirth());
        setField("T_NatConc", person.getDistrictSubdivisionOfBirth());
        setField("T_NatFreg", person.getParishOfBirth());
        setField("T_PaisRes", person.getCountryOfBirth().getCountryNationality().toString());

        setField("T_Morada01", person.getAddress());
        setField("T_Localid01", person.getAreaOfAreaCode());
        setField("T_Telef", person.getDefaultMobilePhoneNumber());

        String postalCode = person.getPostalCode();
        int dashIndex = postalCode.indexOf('-');
        setField("T_CodPos01", postalCode.substring(0, 4));
        String last3Numbers = postalCode.substring(dashIndex + 1, dashIndex + 4);
        setField("T_CodPos03_1", last3Numbers);
        setField("T_Localid02_1", person.getAreaOfAreaCode());

        setField("T_Distrito", person.getDistrictOfResidence());
        setField("T_Conc", person.getDistrictSubdivisionOfResidence());
        setField("T_Freguesia", person.getParishOfResidence());
        setField("T_PaisResid", person.getCountryOfResidence().getName());

        stamper.setFormFlattening(true);
        stamper.close();
        return output;
    }
}
