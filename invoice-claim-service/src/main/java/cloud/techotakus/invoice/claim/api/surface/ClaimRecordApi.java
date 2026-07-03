package cloud.techotakus.invoice.claim.api.surface;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.common.pojo.page.PageResponse;
import cloud.techotakus.invoice.claim.api.model.ClaimDetailResponseModel;
import cloud.techotakus.invoice.claim.api.model.ClaimRecordResponseModel;
import cloud.techotakus.invoice.claim.api.model.ClaimReviewRecordResponseModel;
import cloud.techotakus.invoice.claim.api.model.ClaimSaveRequestModel;
import cloud.techotakus.invoice.claim.api.model.ClaimSubmitRequestModel;
import cloud.techotakus.invoice.claim.api.model.ClaimSubmitResponseModel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RequestMapping("/bff/v1/claims")
public interface ClaimRecordApi {

    @GetMapping
    ResponseEntity<PageResponse<ClaimDetailResponseModel>> claims(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize,
            HttpServletRequest request
    );

    @GetMapping("/list")
    ResponseEntity<PageResponse<ClaimRecordResponseModel>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize,
            HttpServletRequest request
    );

    @GetMapping("/{claimId}")
    ResponseEntity<RestResponse<ClaimDetailResponseModel>> getOne(
            @PathVariable String claimId,
            HttpServletRequest request
    );

    @GetMapping("/{claimId}/review-records")
    ResponseEntity<RestResponse<List<ClaimReviewRecordResponseModel>>> reviewRecords(
            @PathVariable String claimId,
            HttpServletRequest request
    );

    @PostMapping
    ResponseEntity<RestResponse<ClaimSubmitResponseModel>> save(
            @Valid @RequestBody ClaimSaveRequestModel request,
            HttpServletRequest httpRequest
    );

    @PutMapping("/{claimId}")
    ResponseEntity<RestResponse<ClaimSubmitResponseModel>> update(
            @PathVariable String claimId,
            @Valid @RequestBody ClaimSaveRequestModel request,
            HttpServletRequest httpRequest
    );

    @PostMapping("/{claimId}/submit")
    ResponseEntity<RestResponse<ClaimSubmitResponseModel>> submit(
            @PathVariable String claimId,
            @Valid @RequestBody ClaimSubmitRequestModel request,
            HttpServletRequest httpRequest
    );
}
