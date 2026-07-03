package cloud.techotakus.invoice.review.infra.mapper;

import cloud.techotakus.invoice.review.infra.dto.ReviewClaimReviewRecordDto;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReviewClaimReviewRecordMapper extends BaseMapper<ReviewClaimReviewRecordDto> {
}
