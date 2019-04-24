package uk.gov.companieshouse.extensions.api.attachments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.extensions.api.requests.CreatedBy;
import uk.gov.companieshouse.extensions.api.requests.ERICHeaderParser;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.rest.err.Err;
import uk.gov.companieshouse.service.rest.err.Errors;
import uk.gov.companieshouse.service.rest.response.ChResponseBody;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/company/{companyNumber}/extensions/requests")
public class AttachmentsController {

    private PluggableResponseEntityFactory responseEntityFactory;
    private AttachmentsService attachmentsService;
    private ERICHeaderParser ericHeaderParser;

    @Autowired
    public AttachmentsController(PluggableResponseEntityFactory responseEntityFactory,
                                 AttachmentsService attachmentsService,
                                 ERICHeaderParser ericHeaderParser) {
        this.responseEntityFactory = responseEntityFactory;
        this.attachmentsService = attachmentsService;
        this.ericHeaderParser = ericHeaderParser;
    }

    @PostMapping("/{requestId}/reasons/{reasonId}/attachments")
    public ResponseEntity<ChResponseBody<AttachmentDTO>> uploadAttachmentToRequest(
            @RequestParam("file") MultipartFile file, @PathVariable String requestId,
            @PathVariable String reasonId, HttpServletRequest servletRequest) {
        CreatedBy createdBy = new CreatedBy();
        createdBy.setId(ericHeaderParser.getUserId(servletRequest));
        createdBy.setEmail(ericHeaderParser.getEmail(servletRequest));
        createdBy.setForename(ericHeaderParser.getForename(servletRequest));
        createdBy.setSurname(ericHeaderParser.getSurname(servletRequest));

        try {
            ServiceResult<AttachmentDTO> result = attachmentsService.addAttachment(file,
                servletRequest.getRequestURI(), requestId, reasonId);
            return responseEntityFactory.createResponse(result);
        } catch(ServiceException e) {
            Errors errors = new Errors();
            errors.addError(Err.serviceErrBuilder()
                .withError(e.getMessage())
                .build());
            return responseEntityFactory.createResponse(ServiceResult.invalid(errors));
        }
    }

    @DeleteMapping("/{requestId}/reasons/{reasonId}/attachments/{attachmentId}")
    public boolean deleteAttachmentFromRequest(@PathVariable String requestId, @PathVariable String attachmentId) {
      return false;
    }

    @GetMapping("/{requestId}/reasons/{reasonId}/attachments/{attachmentId}")
    public String downloadAttachmentFromRequest(@PathVariable String requestId, @PathVariable String attachmentId) {
      return "Getting attachment";
    }	
}
