package uk.gov.companieshouse.extensions.api.reasons;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestsRepository;
import uk.gov.companieshouse.extensions.api.requests.RequestsService;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.links.Links;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class ReasonsService {

    private RequestsService requestsService;
    private ExtensionRequestsRepository extensionRequestsRepository;
    private ExtensionReasonMapper reasonMapper;
    private Supplier<String> randomUUid;

    @Autowired
    public ReasonsService(RequestsService requestsService,
                          ExtensionRequestsRepository extensionRequestsRepository,
                          ExtensionReasonMapper reasonMapper,
                          Supplier<String> randomUUid) {
        this.requestsService = requestsService;
        this.extensionRequestsRepository = extensionRequestsRepository;
        this.reasonMapper = reasonMapper;
        this.randomUUid = randomUUid;
    }

    public ServiceResult<ExtensionReasonDTO> addExtensionsReasonToRequest(ExtensionCreateReason extensionCreateReason,
                                          String requestId, String requestURI) throws ServiceException {

        ExtensionRequestFullEntity extensionRequestFullEntity = requestsService.getExtensionsRequestById(requestId);
        if (extensionRequestFullEntity == null) {
            throw new ServiceException(String.format("Request %s not found", requestId));
        }
        String uuid = randomUUid.get();

        ExtensionReasonEntityBuilder extensionReasonEntityBuilder =
            ExtensionReasonEntityBuilder
                .builder()
                .withLinks(requestURI)
                .withId(uuid);

        String reason = extensionCreateReason.getReason();
        if (StringUtils.isNotBlank(reason)) {
            extensionReasonEntityBuilder.withReason(reason);
        }

        String additionalText = extensionCreateReason.getAdditionalText();
        if (StringUtils.isNotBlank(additionalText)) {
            extensionReasonEntityBuilder.withAdditionalText(additionalText);
        }

        LocalDate startOn = extensionCreateReason.getStartOn();
        if (startOn != null) {
            extensionReasonEntityBuilder.withStartOn(startOn);
        }

        LocalDate endOn = extensionCreateReason.getEndOn();
        if (endOn != null) {
            extensionReasonEntityBuilder.withEndOn(endOn);
        }

        ExtensionReasonEntity extensionReasonEntity = extensionReasonEntityBuilder.build();

        extensionRequestFullEntity.addReason(extensionReasonEntity);

        ExtensionReasonEntity savedEntity = extensionRequestsRepository
            .save(extensionRequestFullEntity)
            .getReasons()
            .stream()
            .filter(extensionReason -> extensionReason.getId().equals(uuid))
            .findAny()
            .orElseThrow(() -> new ServiceException(String.format("Reason %s not saved in " +
                "database for request %s", uuid, requestId)));

        return ServiceResult.created(reasonMapper.entityToDTO(savedEntity));
    }

    public ExtensionRequestFullEntity removeExtensionsReasonFromRequest(String requestId, String
        reasonId) {

        ExtensionRequestFullEntity extensionRequestFullEntity = requestsService.getExtensionsRequestById(requestId);

        if (!extensionRequestFullEntity.getReasons().isEmpty()) {
            List<ExtensionReasonEntity> extensionRequestReasons = extensionRequestFullEntity
                .getReasons().stream().filter(reason -> !reason.getId().equals(reasonId)).collect(Collectors.toList());

            extensionRequestFullEntity.setReasons(extensionRequestReasons);

            return extensionRequestsRepository.save(extensionRequestFullEntity);
        }
        return extensionRequestFullEntity;
    }
}
