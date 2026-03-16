package com.example.marketsignal.ai;

import com.example.marketsignal.macro.MarketRegime;
import com.example.marketsignal.news.NewsSentiment;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 분석 결과를 사람이 읽기 쉬운 설명으로 변환한다.
 */
@Service
public class AiNarrativeService {

    /**
     * 시장 리포트 요약 문장을 생성한다.
     */
    public String generateReportSummary(MarketRegime marketRegime, List<String> sectors) {
        String sectorSummary = sectors.isEmpty() ? "주도 섹터 부재" : String.join(", ", sectors);
        return switch (marketRegime) {
            case GROWTH -> "금리와 달러가 동반 약세를 보이며 성장주 선호 흐름이 강화되었습니다. 주도 섹터는 " + sectorSummary + " 입니다.";
            case ENERGY -> "원유 급등이 시장의 중심 동인으로 작용하며 에너지 민감 섹터가 우위입니다. 주도 섹터는 " + sectorSummary + " 입니다.";
            case RISK_OFF -> "선물 급락이 위험회피 심리를 자극하며 방어적 대응이 우선되는 장세입니다. 주도 섹터는 " + sectorSummary + " 입니다.";
            case NEUTRAL -> "명확한 한 방향 추세보다 종목별 대응이 중요한 중립 장세입니다. 주도 섹터는 " + sectorSummary + " 입니다.";
        };
    }

    /**
     * 뉴스 감성 및 영향도를 바탕으로 해석 문장을 생성한다.
     */
    public String generateNewsInterpretation(NewsSentiment sentiment, String impact) {
        return switch (sentiment) {
            case POSITIVE -> "단기 수급과 투자심리에 우호적으로 작용할 가능성이 높습니다. 예상 영향도는 " + impact + " 입니다.";
            case NEGATIVE -> "단기 변동성 확대 요인으로 해석되며 보수적 대응이 요구됩니다. 예상 영향도는 " + impact + " 입니다.";
            case NEUTRAL -> "직접적인 방향성 신호는 약하지만, 추가 데이터와 가격 반응 확인이 필요합니다. 예상 영향도는 " + impact + " 입니다.";
        };
    }
}
