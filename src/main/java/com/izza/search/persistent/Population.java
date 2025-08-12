package com.izza.search.persistent;

/**
 * 인구 정보 - 데이터베이스 매핑용 엔티티
 * 테이블: public.population_simple
 */
public class Population {
    
    private String fullCode;        // 법정동 코드
    private String referenceMonth;  // 기준연월
    private String sido;        // 시도명
    private String sig;     // 시군구명
    private String emd; // 읍면동명
    private String ri;          // 리명
    private Integer total;          // 계 (총 인구)
    
    // 연령대별 인구
    private Integer age0to9;        // 0~9세
    private Integer age10to19;      // 10~19세
    private Integer age20to29;      // 20~29세
    private Integer age30to39;      // 30~39세
    private Integer age40to49;      // 40~49세
    private Integer age50to59;      // 50~59세
    private Integer age60to69;      // 60~69세
    private Integer age70to79;      // 70~79세
    private Integer age80plus;      // 80세~
    
    // 성별 인구
    private Integer male;           // 남자
    private Integer female;         // 여자
    
    // 기본 생성자
    public Population() {}
    
    // 전체 생성자
    public Population(String fullCode, String referenceMonth, String sido, String sig,
                     String emd, String ri, Integer total,
                     Integer age0to9, Integer age10to19, Integer age20to29, Integer age30to39,
                     Integer age40to49, Integer age50to59, Integer age60to69, Integer age70to79,
                     Integer age80plus, Integer male, Integer female) {
        this.fullCode = fullCode;
        this.referenceMonth = referenceMonth;
        this.sido = sido;
        this.sig = sig;
        this.emd = emd;
        this.ri = ri;
        this.total = total;
        this.age0to9 = age0to9;
        this.age10to19 = age10to19;
        this.age20to29 = age20to29;
        this.age30to39 = age30to39;
        this.age40to49 = age40to49;
        this.age50to59 = age50to59;
        this.age60to69 = age60to69;
        this.age70to79 = age70to79;
        this.age80plus = age80plus;
        this.male = male;
        this.female = female;
    }
    
    // Getters and Setters
    public String getFullCode() {
        return fullCode;
    }
    
    public void setFullCode(String fullCode) {
        this.fullCode = fullCode;
    }
    
    public String getReferenceMonth() {
        return referenceMonth;
    }
    
    public void setReferenceMonth(String referenceMonth) {
        this.referenceMonth = referenceMonth;
    }
    
    public String getSido() {
        return sido;
    }
    
    public void setSido(String sido) {
        this.sido = sido;
    }
    
    public String getSig() {
        return sig;
    }
    
    public void setSig(String sig) {
        this.sig = sig;
    }
    
    public String getEmd() {
        return emd;
    }
    
    public void setEmd(String emd) {
        this.emd = emd;
    }
    
    public String getRi() {
        return ri;
    }
    
    public void setRi(String ri) {
        this.ri = ri;
    }
    
    public Integer getTotal() {
        return total;
    }
    
    public void setTotal(Integer total) {
        this.total = total;
    }
    
    public Integer getAge0to9() {
        return age0to9;
    }
    
    public void setAge0to9(Integer age0to9) {
        this.age0to9 = age0to9;
    }
    
    public Integer getAge10to19() {
        return age10to19;
    }
    
    public void setAge10to19(Integer age10to19) {
        this.age10to19 = age10to19;
    }
    
    public Integer getAge20to29() {
        return age20to29;
    }
    
    public void setAge20to29(Integer age20to29) {
        this.age20to29 = age20to29;
    }
    
    public Integer getAge30to39() {
        return age30to39;
    }
    
    public void setAge30to39(Integer age30to39) {
        this.age30to39 = age30to39;
    }
    
    public Integer getAge40to49() {
        return age40to49;
    }
    
    public void setAge40to49(Integer age40to49) {
        this.age40to49 = age40to49;
    }
    
    public Integer getAge50to59() {
        return age50to59;
    }
    
    public void setAge50to59(Integer age50to59) {
        this.age50to59 = age50to59;
    }
    
    public Integer getAge60to69() {
        return age60to69;
    }
    
    public void setAge60to69(Integer age60to69) {
        this.age60to69 = age60to69;
    }
    
    public Integer getAge70to79() {
        return age70to79;
    }
    
    public void setAge70to79(Integer age70to79) {
        this.age70to79 = age70to79;
    }
    
    public Integer getAge80plus() {
        return age80plus;
    }
    
    public void setAge80plus(Integer age80plus) {
        this.age80plus = age80plus;
    }
    
    public Integer getMale() {
        return male;
    }
    
    public void setMale(Integer male) {
        this.male = male;
    }
    
    public Integer getFemale() {
        return female;
    }
    
    public void setFemale(Integer female) {
        this.female = female;
    }
    
    @Override
    public String toString() {
        return "Population{" +
                "fullCode='" + fullCode + '\'' +
                ", referenceMonth='" + referenceMonth + '\'' +
                ", sido='" + sido + '\'' +
                ", sig='" + sig + '\'' +
                ", emd='" + emd + '\'' +
                ", ri='" + ri + '\'' +
                ", total=" + total +
                ", age0to9=" + age0to9 +
                ", age10to19=" + age10to19 +
                ", age20to29=" + age20to29 +
                ", age30to39=" + age30to39 +
                ", age40to49=" + age40to49 +
                ", age50to59=" + age50to59 +
                ", age60to69=" + age60to69 +
                ", age70to79=" + age70to79 +
                ", age80plus=" + age80plus +
                ", male=" + male +
                ", female=" + female +
                '}';
    }
}
