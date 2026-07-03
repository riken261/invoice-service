package cloud.techotakus.invoice.review.api.surface;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.review.api.model.ReviewClaimReviewRecordResponseModel;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/bff/v1/claims")
public interface ReviewClaimRecordApi {

    @GetMapping("/{claimId}/review-records")
    ResponseEntity<RestResponse<List<ReviewClaimReviewRecordResponseModel>>> reviewRecords(
            @PathVariable String claimId,
            HttpServletRequest request
    );
}
