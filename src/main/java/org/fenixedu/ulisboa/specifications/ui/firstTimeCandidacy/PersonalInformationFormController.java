/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and Serviços Partilhados da
 * Universidade de Lisboa:
 *  - Copyright © 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright © 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: joao.roxo@qub-it.com 
 *               nuno.pinheiro@qub-it.com
 *
 * 
 * This file is part of FenixEdu Specifications.
 *
 * FenixEdu Specifications is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Specifications is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Specifications.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.ulisboa.specifications.ui.firstTimeCandidacy;

import static org.fenixedu.bennu.FenixeduUlisboaSpecificationsSpringConfiguration.BUNDLE;
import static org.fenixedu.ulisboa.specifications.ui.firstTimeCandidacy.util.CitzenCardValidation.validateNumeroDocumentoCC;
import static org.fenixedu.ulisboa.specifications.ui.firstTimeCandidacy.util.FiscalCodeValidation.isValidcontrib;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.fenixedu.academic.domain.Country;
import org.fenixedu.academic.domain.GrantOwnerType;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.ProfessionType;
import org.fenixedu.academic.domain.ProfessionalSituationConditionType;
import org.fenixedu.academic.domain.organizationalStructure.Party;
import org.fenixedu.academic.domain.organizationalStructure.PartySocialSecurityNumber;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.academic.domain.organizationalStructure.UnitName;
import org.fenixedu.academic.domain.person.Gender;
import org.fenixedu.academic.domain.person.IDDocumentType;
import org.fenixedu.academic.domain.person.MaritalStatus;
import org.fenixedu.academic.domain.raides.DegreeDesignation;
import org.fenixedu.academic.domain.student.PersonalIngressionData;
import org.fenixedu.academic.predicate.AccessControl;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.bennu.spring.portal.BennuSpringController;
import org.fenixedu.commons.StringNormalizer;
import org.fenixedu.ulisboa.specifications.ULisboaConfiguration;
import org.fenixedu.ulisboa.specifications.domain.PersonUlisboaSpecifications;
import org.fenixedu.ulisboa.specifications.domain.ProfessionTimeType;
import org.fenixedu.ulisboa.specifications.ui.FenixeduUlisboaSpecificationsBaseController;
import org.fenixedu.ulisboa.specifications.ui.firstTimeCandidacy.util.UnitBean;
import org.joda.time.LocalDate;
import org.joda.time.YearMonthDay;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;

@BennuSpringController(value = FirstTimeCandidacyController.class)
@RequestMapping(PersonalInformationFormController.CONTROLLER_URL)
public class PersonalInformationFormController extends FenixeduUlisboaSpecificationsBaseController {

    private static final String IDENTITY_CARD_CONTROL_DIGIT_FORMAT = "[0-9]";

    private static final String CITZEN_CARD_CHECK_DIGIT_FORMAT = "[0-9][a-zA-Z][a-zA-Z][0-9]";

    private static final String SOCIAL_SECURITY_NUMBER_FORMAT = "\\d{9}";

    public static final String CONTROLLER_URL = "/fenixedu-ulisboa-specifications/firsttimecandidacy/personalinformationform";

    private static final String _FILLPERSONALINFORMATION_URI = "/fillpersonalinformation";
    public static final String FILLPERSONALINFORMATION_URL = CONTROLLER_URL + _FILLPERSONALINFORMATION_URI;

    @RequestMapping
    public String home(Model model) {
        return "forward:" + CONTROLLER_URL + _FILLPERSONALINFORMATION_URI;
    }

    @RequestMapping(value = "/back", method = RequestMethod.GET)
    public String back(Model model, RedirectAttributes redirectAttributes) {
        return redirect(FirstTimeCandidacyController.CONTROLLER_URL, model, redirectAttributes);
    }

    @RequestMapping(value = _FILLPERSONALINFORMATION_URI, method = RequestMethod.GET)
    public String fillpersonalinformation(Model model, RedirectAttributes redirectAttributes) {
        if (!FirstTimeCandidacyController.isPeriodOpen()) {
            return redirect(FirstTimeCandidacyController.CONTROLLER_URL, model, redirectAttributes);
        }
        model.addAttribute("genderValues", Gender.values());

        List<MaritalStatus> maritalStatusValues = new ArrayList<>();
        maritalStatusValues.addAll(Arrays.asList(MaritalStatus.values()));
        maritalStatusValues.remove(MaritalStatus.UNKNOWN);
        model.addAttribute("maritalStatusValues", maritalStatusValues);
        model.addAttribute("professionalConditionValues", ProfessionalSituationConditionType.values());
        model.addAttribute("professionTypeValues", ProfessionType.values());
        model.addAttribute("professionTimeTypeValues", ProfessionTimeType.readAll().collect(Collectors.toList()));
        model.addAttribute("grantOwnerTypeValues", GrantOwnerType.values());

        model.addAttribute("placingOption", FirstTimeCandidacyController.getCandidacy().getPlacingOption());

        PersonalInformationForm form = fillFormIfRequired(model);
        model.addAttribute("personalInformationForm", form);
        if (!form.getIsForeignStudent()) {
            ArrayList<IDDocumentType> idDocumentTypeValues = new ArrayList<>();
            idDocumentTypeValues.add(IDDocumentType.CITIZEN_CARD);
            idDocumentTypeValues.add(IDDocumentType.IDENTITY_CARD);
            model.addAttribute("idDocumentTypeValues", idDocumentTypeValues);
        } else {
            model.addAttribute("idDocumentTypeValues", IDDocumentType.values());
        }
        addInfoMessage(BundleUtil.getString(BUNDLE, "label.firstTimeCandidacy.fillPersonalInformation.info"), model);
        return "fenixedu-ulisboa-specifications/firsttimecandidacy/personalinformationform/fillpersonalinformation";
    }

    private PersonalInformationForm fillFormIfRequired(Model model) {
        Person person = AccessControl.getPerson();
        PersonalInformationForm form = (PersonalInformationForm) model.asMap().get("personalInformationForm");
        if (form != null) {
            if (!form.getIsForeignStudent()) {
                form.setDocumentIdNumber(person.getDocumentIdNumber());
                form.setIdDocumentType(person.getIdDocumentType());
            }
            model.addAttribute("personalInformationForm", form);
            return form;
        }

        form = new PersonalInformationForm();

        form.setDocumentIdEmissionLocation(person.getEmissionLocationOfDocumentId());
        YearMonthDay emissionDateOfDocumentIdYearMonthDay = person.getEmissionDateOfDocumentIdYearMonthDay();
        YearMonthDay expirationDateOfDocumentIdYearMonthDay = person.getExpirationDateOfDocumentIdYearMonthDay();
        form.setDocumentIdEmissionDate(emissionDateOfDocumentIdYearMonthDay != null ? new LocalDate(person
                .getEmissionDateOfDocumentIdYearMonthDay().toDateMidnight()) : null);
        form.setDocumentIdExpirationDate(expirationDateOfDocumentIdYearMonthDay != null ? new LocalDate(
                expirationDateOfDocumentIdYearMonthDay.toDateMidnight()) : null);

        form.setSocialSecurityNumber(person.getSocialSecurityNumber());
        form.setMaritalStatus(person.getMaritalStatus());
        form.setProfession(person.getProfession());

        PersonUlisboaSpecifications personUl = person.getPersonUlisboaSpecifications();
        if (personUl != null) {
            if (!StringUtils.equals(personUl.getDgesTempIdCode(), person.getDocumentIdNumber())) {
                form.setDocumentIdNumber(person.getDocumentIdNumber());
                form.setIdDocumentType(person.getIdDocumentType());
            }

            form.setProfessionTimeType(personUl.getProfessionTimeType());

            Unit institution = personUl.getFirstOptionInstitution();
            if (institution != null) {
                form.setFirstOptionInstitution(institution);

                String degreeDesignationName = personUl.getFirstOptionDegreeDesignation();
                Predicate<DegreeDesignation> matchesName = dd -> dd.getDescription().equalsIgnoreCase(degreeDesignationName);
                DegreeDesignation degreeDesignation =
                        institution.getDegreeDesignationSet().stream().filter(matchesName).findFirst().orElse(null);
                form.setFirstOptionDegreeDesignation(degreeDesignation);
            }
        } else {
            form.setDocumentIdNumber(person.getDocumentIdNumber());
            form.setIdDocumentType(person.getIdDocumentType());
        }

        PersonalIngressionData personalData =
                FirstTimeCandidacyController.getOrCreatePersonalIngressionData(FirstTimeCandidacyController.getCandidacy()
                        .getPrecedentDegreeInformation());
        form.setMaritalStatus(personalData.getMaritalStatus());
        if (form.getMaritalStatus() == null) {
            form.setMaritalStatus(MaritalStatus.SINGLE);
        }
        form.setProfessionType(personalData.getProfessionType());
        if (form.getProfessionType() == null) {
            form.setProfessionType(ProfessionType.OTHER);
        }
        form.setGrantOwnerType(personalData.getGrantOwnerType());
        if (form.getGrantOwnerType() == null) {
            form.setGrantOwnerType(GrantOwnerType.STUDENT_WITHOUT_SCHOLARSHIP);
        }
        Unit grantOwnerProvider = personalData.getGrantOwnerProvider();
        form.setGrantOwnerProvider(grantOwnerProvider != null ? grantOwnerProvider.getExternalId() : null);
        form.setProfessionalCondition(personalData.getProfessionalCondition());
        if (form.getProfessionalCondition() == null) {
            form.setProfessionalCondition(ProfessionalSituationConditionType.STUDENT);
        }

        return form;
    }

    @RequestMapping(value = _FILLPERSONALINFORMATION_URI, method = RequestMethod.POST)
    public String fillpersonalinformation(PersonalInformationForm form, Model model, RedirectAttributes redirectAttributes) {
        if (!FirstTimeCandidacyController.isPeriodOpen()) {
            return redirect(FirstTimeCandidacyController.CONTROLLER_URL, model, redirectAttributes);
        }
        if (!validate(form, model)) {
            return fillpersonalinformation(model, redirectAttributes);
        }

        try {
            writeData(form);
            model.addAttribute("personalInformationForm", form);
            return redirect(FiliationFormController.FILLFILIATION_URL, model, redirectAttributes);
        } catch (Exception de) {
            addErrorMessage(BundleUtil.getString(BUNDLE, "label.error.create") + de.getLocalizedMessage(), model);
            LoggerFactory.getLogger(this.getClass()).error("Exception for user " + AccessControl.getPerson().getUsername());
            de.printStackTrace();
            return fillpersonalinformation(model, redirectAttributes);
        }
    }

    private boolean validate(PersonalInformationForm form, Model model) {
        Person person = AccessControl.getPerson();
        IDDocumentType idType = form.getIdDocumentType();
        if (idType == null) {
            addErrorMessage(BundleUtil.getString(BUNDLE, "error.documentIdType.required"), model);
            return false;
        }
        if (form.getIsForeignStudent()) {
            if (StringUtils.isEmpty(form.getDocumentIdNumber())) {
                addErrorMessage(BundleUtil.getString(BUNDLE, "error.documentIdNumber.required"), model);
                return false;
            }

        }

        if (!form.getIsForeignStudent()) {
            if (StringUtils.isEmpty(form.getSocialSecurityNumber())
                    || !form.getSocialSecurityNumber().matches(SOCIAL_SECURITY_NUMBER_FORMAT)) {
                addErrorMessage(BundleUtil.getString(BUNDLE,
                        "error.candidacy.workflow.PersonalInformationForm.incorrect.socialSecurityNumber"), model);
                return false;
            }
        } else {
            if (!StringUtils.isEmpty(form.getSocialSecurityNumber())
                    && !form.getSocialSecurityNumber().matches(SOCIAL_SECURITY_NUMBER_FORMAT)) {
                addErrorMessage(BundleUtil.getString(BUNDLE,
                        "error.candidacy.workflow.PersonalInformationForm.incorrect.socialSecurityNumber"), model);
                return false;
            }
        }

        if (form.getDocumentIdExpirationDate() == null) {
            addErrorMessage(BundleUtil.getString(BUNDLE, "error.expirationDate.required"), model);
            return false;
        }

        if (!StringUtils.isEmpty(form.getSocialSecurityNumber()) && !isValidcontrib(form.getSocialSecurityNumber())) {
            addErrorMessage(
                    BundleUtil.getString(BUNDLE, "error.candidacy.workflow.PersonalInformationForm.socialSecurityNumber.invalid"),
                    model);
            return false;
        }
        if (!PartySocialSecurityNumber.DEFAULT_SOCIAL_SECURITY_NUMBER.equals(form.getSocialSecurityNumber())) {
            Party party = PartySocialSecurityNumber.readPartyBySocialSecurityNumber(form.getSocialSecurityNumber());
            if (party != null && party != person) {
                addErrorMessage(BundleUtil.getString(BUNDLE,
                        "error.candidacy.workflow.PersonalInformationForm.socialSecurityNumber.already.exists"), model);
                return false;
            }
        }

        if (form.isStudentWorking()) {
            if (StringUtils.isEmpty(form.getProfession())) {
                addErrorMessage(
                        BundleUtil.getString(BUNDLE, "error.candidacy.workflow.PersonalInformationForm.profession.required"),
                        model);
                return false;
            }
            if (form.getProfessionTimeType() == null) {
                addErrorMessage(BundleUtil.getString(BUNDLE,
                        "error.candidacy.workflow.PersonalInformationForm.professionTimeType.required"), model);
                return false;
            }
        }

        GrantOwnerType grantOwnerType = form.getGrantOwnerType();
        if (grantOwnerType.equals(GrantOwnerType.OTHER_INSTITUTION_GRANT_OWNER)
                || grantOwnerType.equals(GrantOwnerType.ORIGIN_COUNTRY_GRANT_OWNER)) {
            if (StringUtils.isEmpty(form.getGrantOwnerProvider())) {
                addErrorMessage(BundleUtil.getString(BUNDLE,
                        "error.candidacy.workflow.PersonalInformationForm.grant.owner.must.choose.granting.institution"), model);
                return false;
            }
        }

        return true;
    }

    private boolean testsMode() {
        Boolean devMode = CoreConfiguration.getConfiguration().developmentMode();
        Boolean qualityMode = ULisboaConfiguration.getConfiguration().isQualityMode();
        return (devMode != null && devMode == true) || (qualityMode != null && qualityMode == true);
    }

    @Atomic
    private void writeData(PersonalInformationForm form) {
        Person person = AccessControl.getPerson();
        PersonUlisboaSpecifications personUl = PersonUlisboaSpecifications.findOrCreate(person);
        PersonalIngressionData personalData =
                FirstTimeCandidacyController.getOrCreatePersonalIngressionData(FirstTimeCandidacyController.getCandidacy()
                        .getPrecedentDegreeInformation());

        person.setEmissionLocationOfDocumentId(form.getDocumentIdEmissionLocation());
        LocalDate documentIdEmissionDate = form.getDocumentIdEmissionDate();
        LocalDate documentIdExpirationDate = form.getDocumentIdExpirationDate();
        person.setEmissionDateOfDocumentIdYearMonthDay(documentIdEmissionDate != null ? new YearMonthDay(documentIdEmissionDate
                .toDate()) : null);
        person.setExpirationDateOfDocumentIdYearMonthDay(documentIdExpirationDate != null ? new YearMonthDay(
                documentIdExpirationDate.toDate()) : null);

        String socialSecurityNumber = form.getSocialSecurityNumber();
        if (StringUtils.isEmpty(socialSecurityNumber)) {
            socialSecurityNumber = PartySocialSecurityNumber.DEFAULT_SOCIAL_SECURITY_NUMBER;
        }
        person.setSocialSecurityNumber(socialSecurityNumber);

        person.setMaritalStatus(form.getMaritalStatus());
        personalData.setMaritalStatus(form.getMaritalStatus());

        if (1 < FirstTimeCandidacyController.getCandidacy().getPlacingOption()) {
            personUl.setFirstOptionInstitution(form.getFirstOptionInstitution());
            if (form.getFirstOptionDegreeDesignation() != null) {
                personUl.setFirstOptionDegreeDesignation(form.getFirstOptionDegreeDesignation().getDescription());
            }
        }

        personalData.setProfessionalCondition(form.getProfessionalCondition());
        person.setProfession(form.getProfession());
        personalData.setProfessionType(form.getProfessionType());
        personUl.setProfessionTimeType(form.getProfessionTimeType());

        person.setIdDocumentType(form.getIdDocumentType());
        if (form.getIsForeignStudent()) {
            person.setDocumentIdNumber(form.getDocumentIdNumber());
            personUl.setDgesTempIdCode("");
        }

        GrantOwnerType grantOwnerType = form.getGrantOwnerType();
        personalData.setGrantOwnerType(grantOwnerType);
        if (grantOwnerType != null && !grantOwnerType.equals(GrantOwnerType.STUDENT_WITHOUT_SCHOLARSHIP)) {
            Unit grantOwnerProvider = FenixFramework.getDomainObject(form.getGrantOwnerProvider());
            if (grantOwnerProvider == null
                    && (grantOwnerType == GrantOwnerType.OTHER_INSTITUTION_GRANT_OWNER || grantOwnerType == GrantOwnerType.ORIGIN_COUNTRY_GRANT_OWNER)) {
                //We accept new institutions for these 2 cases
                grantOwnerProvider = Unit.createNewNoOfficialExternalInstitution(form.getGrantOwnerProvider());
            }
            personalData.setGrantOwnerProvider(grantOwnerProvider);
        } else {
            personalData.setGrantOwnerProvider(null);
        }
    }

    @RequestMapping(value = "/externalUnit", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<UnitBean> readExternalUnits(@RequestParam("namePart") String namePart, Model model) {
        Function<UnitName, UnitBean> createUnitBean = un -> new UnitBean(un.getUnit().getExternalId(), un.getUnit().getName());
        return UnitName.findExternalUnit(namePart, 50).stream().map(createUnitBean).collect(Collectors.toList());
    }

    @RequestMapping(value = "/externalUnitFreeOption", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<UnitBean> readExternalUnitsWithFreeOption(@RequestParam("namePart") String namePart, Model model) {
        List<UnitBean> readExternalUnits = readExternalUnits(namePart, model);
        readExternalUnits.add(0, new UnitBean(namePart, namePart));
        return readExternalUnits;
    }

    @RequestMapping(value = "/academicUnit", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<UnitBean> readAcademicUnits(@RequestParam("namePart") String namePart, Model model) {
        Function<UnitName, UnitBean> createUnitBean = un -> new UnitBean(un.getUnit().getExternalId(), un.getUnit().getName());
        return UnitName.findExternalAcademicUnit(namePart, 50).stream().map(createUnitBean).collect(Collectors.toList());
    }

    @RequestMapping(value = "/degreeDesignation/{unit}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody Collection<DegreeDesignationBean> readExternalUnits(@PathVariable("unit") String unitOid,
            @RequestParam("namePart") String namePart, Model model) {
        Unit unit = null;
        try {
            unit = FenixFramework.getDomainObject(unitOid);
        } catch (Exception e) {
            //Not a unit, so it is a custom value, ignore
        }

        Collection<DegreeDesignation> possibleDesignations;
        if (unit == null) {
            possibleDesignations = Bennu.getInstance().getDegreeDesignationsSet();
        } else {
            possibleDesignations = unit.getDegreeDesignationSet();
        }

        Predicate<DegreeDesignation> matchesName =
                dd -> StringNormalizer.normalize(getFullDescription(dd)).contains(StringNormalizer.normalize(namePart));
        Function<DegreeDesignation, DegreeDesignationBean> createDesignationBean =
                dd -> new DegreeDesignationBean(getFullDescription(dd), dd.getExternalId());
        return possibleDesignations.stream().filter(matchesName).map(createDesignationBean).limit(50)
                .collect(Collectors.toList());
    }

    private static String getFullDescription(DegreeDesignation designation) {
        return designation.getDegreeClassification().getDescription1() + " - " + designation.getDescription();
    }

    public static class DegreeDesignationBean {
        private final String degreeDesignationText;
        private final String degreeDesignationId;

        public DegreeDesignationBean(String degreeDesignationText, String degreeDesignationId) {
            super();
            this.degreeDesignationText = degreeDesignationText;
            this.degreeDesignationId = degreeDesignationId;
        }

        public String getDegreeDesignationText() {
            return degreeDesignationText;
        }

        public String getDegreeDesignationId() {
            return degreeDesignationId;
        }
    }

    public static class PersonalInformationForm implements Serializable {
        private static final long serialVersionUID = 1L;

        //can be either the series number or the extra digit
        private String identificationDocumentSeriesNumber;
        private String documentIdEmissionLocation;
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate documentIdEmissionDate;
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private LocalDate documentIdExpirationDate;
        private String socialSecurityNumber;
        private MaritalStatus maritalStatus;
        private ProfessionalSituationConditionType professionalCondition;
        private String profession;
        private ProfessionType professionType;
        private ProfessionTimeType professionTimeType;
        private GrantOwnerType grantOwnerType;
        private String grantOwnerProvider;

        private Unit firstOptionInstitution;
        private DegreeDesignation firstOptionDegreeDesignation;
        private String documentIdNumber;

        private IDDocumentType idDocumentType;

        public String getName() {
            return AccessControl.getPerson().getName();
        }

        public String getUsername() {
            return AccessControl.getPerson().getUsername();
        }

        public Gender getGender() {
            return AccessControl.getPerson().getGender();
        }

        public String getDocumentIdNumber() {
            return documentIdNumber;
        }

        public void setDocumentIdNumber(String documentIdNumber) {
            this.documentIdNumber = documentIdNumber;
        }

        public IDDocumentType getIdDocumentType() {
            return idDocumentType;
        }

        public void setIdDocumentType(IDDocumentType idDocumentType) {
            this.idDocumentType = idDocumentType;
        }

        public String getDocumentIdEmissionLocation() {
            return documentIdEmissionLocation;
        }

        public void setDocumentIdEmissionLocation(String documentIdEmissionLocation) {
            this.documentIdEmissionLocation = documentIdEmissionLocation;
        }

        public LocalDate getDocumentIdEmissionDate() {
            return documentIdEmissionDate;
        }

        public void setDocumentIdEmissionDate(LocalDate documentIdEmissionDate) {
            this.documentIdEmissionDate = documentIdEmissionDate;
        }

        public LocalDate getDocumentIdExpirationDate() {
            return documentIdExpirationDate;
        }

        public void setDocumentIdExpirationDate(LocalDate documentIdExpirationDate) {
            this.documentIdExpirationDate = documentIdExpirationDate;
        }

        public String getSocialSecurityNumber() {
            return socialSecurityNumber;
        }

        public void setSocialSecurityNumber(String socialSecurityNumber) {
            this.socialSecurityNumber = socialSecurityNumber;
        }

        public ProfessionType getProfessionType() {
            return professionType;
        }

        public void setProfessionType(ProfessionType professionType) {
            this.professionType = professionType;
        }

        public ProfessionalSituationConditionType getProfessionalCondition() {
            return professionalCondition;
        }

        public void setProfessionalCondition(ProfessionalSituationConditionType professionalCondition) {
            this.professionalCondition = professionalCondition;
        }

        public String getProfession() {
            return profession;
        }

        public void setProfession(String profession) {
            this.profession = profession;
        }

        public MaritalStatus getMaritalStatus() {
            return maritalStatus;
        }

        public void setMaritalStatus(MaritalStatus maritalStatus) {
            this.maritalStatus = maritalStatus;
        }

        public GrantOwnerType getGrantOwnerType() {
            return grantOwnerType;
        }

        public void setGrantOwnerType(GrantOwnerType grantOwnerType) {
            this.grantOwnerType = grantOwnerType;
        }

        public String getGrantOwnerProvider() {
            return grantOwnerProvider;
        }

        public void setGrantOwnerProvider(String grantOwnerProvider) {
            this.grantOwnerProvider = grantOwnerProvider;
        }

        public static long getSerialversionuid() {
            return serialVersionUID;
        }

        public ProfessionTimeType getProfessionTimeType() {
            return professionTimeType;
        }

        public void setProfessionTimeType(ProfessionTimeType professionTimeType) {
            this.professionTimeType = professionTimeType;
        }

        public boolean isStudentWorking() {
            if (isWorkingCondition()) {
                return true;
            }
            if (!StringUtils.isEmpty(getProfession())) {
                return true;
            }
            if (getProfessionTimeType() != null) {
                return true;
            }
            if (isWorkingProfessionType()) {
                return true;
            }
            return false;
        }

        private boolean isWorkingCondition() {
            switch (getProfessionalCondition()) {
            case WORKS_FOR_OTHERS:
                return true;
            case EMPLOYEER:
                return true;
            case INDEPENDENT_WORKER:
                return true;
            case WORKS_FOR_FAMILY_WITHOUT_PAYMENT:
                return true;
            case HOUSEWIFE:
                return true;
            case MILITARY_SERVICE:
                return true;
            default:
                return false;
            }
        }

        private boolean isWorkingProfessionType() {
            switch (getProfessionType()) {
            case UNKNOWN:
                return false;
            case OTHER:
                return false;
            default:
                return true;
            }
        }

        public Unit getFirstOptionInstitution() {
            return firstOptionInstitution;
        }

        public void setFirstOptionInstitution(Unit firstOptionInstitution) {
            this.firstOptionInstitution = firstOptionInstitution;
        }

        public DegreeDesignation getFirstOptionDegreeDesignation() {
            return firstOptionDegreeDesignation;
        }

        public void setFirstOptionDegreeDesignation(DegreeDesignation firstOptionDegreeDesignation) {
            this.firstOptionDegreeDesignation = firstOptionDegreeDesignation;
        }

        public String getGrantOwnerProviderName() {
            Unit unit = FenixFramework.getDomainObject(getGrantOwnerProvider());
            if (unit == null) {
                return getGrantOwnerProvider();
            } else {
                return unit.getName();
            }
        }

        public boolean getIsForeignStudent() {
            Country nationality = AccessControl.getPerson().getCountry();
            return nationality == null || !nationality.isDefaultCountry();
        }
    }
}
