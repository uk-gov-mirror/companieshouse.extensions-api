package uk.gov.companieshouse.extensions.api.requests;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.service.links.Links;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("${api.endpoint.extensions}")
public class RequestsController {

    @Autowired
    private RequestsService requestsService;

    @Autowired
    private ExtensionRequestsRepository extensionRequestsRepository;

    @Autowired
    private Supplier<LocalDateTime> dateTimeSupplierNow;

    @PostMapping("/")
    public ResponseEntity<ExtensionRequest> createExtensionRequestResource(@RequestBody ExtensionCreateRequest extensionCreateRequest,
                                                                           HttpServletRequest request) {
        UUID uuid = UUID.randomUUID();
        String linkToSelf = request.getRequestURI() + uuid;

        ExtensionRequest extensionRequest = new ExtensionRequest();
        extensionRequest.setId(uuid);
        extensionRequest.setUser(extensionCreateRequest.getUser());
        extensionRequest.setStatus(RequestStatus.OPEN);
        extensionRequest.setRequestDate(dateTimeSupplierNow.get());
        extensionRequest.setAccountingPeriodStartDate(extensionCreateRequest.getAccountingPeriodStartDate());
        extensionRequest.setAccountingPeriodEndDate(extensionCreateRequest.getAccountingPeriodEndDate());
        Links links = new Links();
        links.setLink(() ->  "self", linkToSelf);
        extensionRequest.setLinks(links);

        extensionRequestsRepository.insert(extensionRequest);

        return ResponseEntity.created(URI.create(linkToSelf)).body(extensionRequest);
    }

    @GetMapping("/")
    public List<ExtensionRequest> getExtensionRequestsList() {
        ExtensionRequest er = new ExtensionRequest();
        er.setUser("user one");
      return Arrays.asList(er);
    }

    @GetMapping("/{requestId}")
    public ExtensionRequest getSingleExtensionRequestById(@PathVariable String requestId) {
        return requestsService.getExtensionsRequestById(requestId);
    }

    @DeleteMapping("/{requestId}")
    public boolean deleteExtensionRequestById(@PathVariable String requestId) {
      return false;
    }
}
