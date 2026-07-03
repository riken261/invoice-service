package cloud.techotakus.invoice.review.api.surface;

import cloud.techotakus.common.pojo.page.PageResponse;
import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.review.api.model.ReviewClaimDecisionRequestModel;
import cloud.techotakus.invoice.review.api.model.ReviewClaimDecisionResponseModel;
import cloud.techotakus.invoice.review.api.model.ReviewClaimDetailResponseModel;
import cloud.techotakus.invoice.review.api.model.ReviewClaimPendingResponseModel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/bff/v1/finance/claims")
public interface ReviewClaimApi {

    @GetMapping("/{claimId}")
    ResponseEntity<RestResponse<ReviewClaimDetailResponseModel>> getOne(
            @org.springframework.web.bind.annotation.PathVariable String claimId,
            HttpServletRequest request
    );

    @PostMapping("/{claimId}/reject")
    ResponseEntity<RestResponse<ReviewClaimDecisionResponseModel>> reject(
            @org.springframework.web.bind.annotation.PathVariable String claimId,
            @Valid @RequestBody ReviewClaimDecisionRequestModel request,
            HttpServletRequest httpRequest
    );

    @PostMapping("/{claimId}/approve")
    ResponseEntity<RestResponse<ReviewClaimDecisionResponseModel>> approve(
            @org.springframework.web.bind.annotation.PathVariable String claimId,
            @Valid @RequestBody ReviewClaimDecisionRequestModel request,
            HttpServletRequest httpRequest
    );

    @GetMapping("/pending")
    ResponseEntity<PageResponse<ReviewClaimPendingResponseModel>> pending(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize,
            HttpServletRequest request
    );
}
