package com.imooc.coupon.vo;

import com.imooc.coupon.constant.PeriodType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateRule {

    private Expiration expiration;
    private Discount discount;
    private Integer limitation;
    private Usage usage;
    private String weight;

    public boolean validate() {
        return expiration.validate() && discount.validate() && limitation > 0
                && usage.validate() && StringUtils.isNotEmpty(weight);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Expiration {
        private Integer period;
        private Integer gap;
        private Long deadline;

        boolean validate() {
            return null != PeriodType.of(period) && gap > 0 && deadline > 0;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Discount {
        private Integer quota;
        private Integer base;

        boolean validate() {
            return quota > 0 && base > 0;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        private String province;
        private String city;
        private String goodsType;

        boolean validate() {
            return StringUtils.isNotEmpty(province) && StringUtils.isNotEmpty(city) && StringUtils.isNotEmpty(goodsType);
        }
    }
}
