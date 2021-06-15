package ca.uhn.fhir.jpa.starter.dotBase.services;

import ca.uhn.fhir.jpa.starter.dotBase.DotbaseProperties;
import ca.uhn.fhir.jpa.starter.dotBase.utils.ExtensionUtils;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.consent.ConsentOutcome;
import ca.uhn.fhir.rest.server.interceptor.consent.IConsentContextServices;
import ca.uhn.fhir.rest.server.interceptor.consent.IConsentService;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Procedure.ProcedureStatus;

public class Authorization implements IConsentService {
  private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(
    Authorization.class
  );

  /**
   * Invoked once at the start of every request
   */
  @Override
  public ConsentOutcome startOperation(
    RequestDetails theRequestDetails,
    IConsentContextServices theContextServices
  ) {
    return ConsentOutcome.PROCEED;
  }

  /**
   * Can a given resource be returned to the user?
   */
  @Override
  public ConsentOutcome canSeeResource(
    RequestDetails theRequestDetails,
    IBaseResource theResource,
    IConsentContextServices theContextServices
  ) {
    if (theRequestDetails.getRequestType() == RequestTypeEnum.GET && isDraft(theResource)) {
      return isAuthorizedRequester(theRequestDetails, theResource)
        ? ConsentOutcome.AUTHORIZED
        : ConsentOutcome.REJECT;
    }
    return ConsentOutcome.AUTHORIZED;
  }

  /**
   * Modify resources that are being shown to the user
   */
  @Override
  public ConsentOutcome willSeeResource(
    RequestDetails theRequestDetails,
    IBaseResource theResource,
    IConsentContextServices theContextServices
  ) {
    return ConsentOutcome.AUTHORIZED;
  }

  private static boolean isDraft(IBaseResource theResource) {
    String theExtensionUrl = DotbaseProperties.get("Url.StructureDefinition.DocumentDraftAction");
    return ExtensionUtils.hasExtension(theResource, theExtensionUrl);
  }

  private boolean isAuthorizedRequester(
    RequestDetails theRequestDetails,
    IBaseResource theResource
  ) {
    String requestingUser = (String) theRequestDetails.getAttribute("_username");
    return (
      isResourceCreator(theResource, requestingUser) ||
      isResourceEditor(theResource, requestingUser)
    );
  }

  private static boolean isResourceCreator(IBaseResource theResource, String requestingUser) {
    return (
      theResource
        .getMeta()
        .getTag(DotbaseProperties.get("Url.NamingSystem.DotbaseUsername"), requestingUser) !=
      null
    );
  }

  private static boolean isResourceEditor(IBaseResource theResource, String requestingUser) {
    String theExtensionUrl = DotbaseProperties.get("Url.StructureDefinition.ResourceEditor");
    return ExtensionUtils.hasExtension((IBase) theResource, theExtensionUrl, requestingUser);
  }
}
