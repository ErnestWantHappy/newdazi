package com.ruoyi.business.domain.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Python 双 Sheet Excel 在浏览器解析后的受控导入对象。 */
public class PythonQuestionImportRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<QuestionRow> questions = new ArrayList<QuestionRow>();
    private List<TestCaseRow> testCases = new ArrayList<TestCaseRow>();
    public List<QuestionRow> getQuestions() { return questions; }
    public void setQuestions(List<QuestionRow> v) { questions = v; }
    public List<TestCaseRow> getTestCases() { return testCases; }
    public void setTestCases(List<TestCaseRow> v) { testCases = v; }

    public static class QuestionRow implements Serializable {
        private static final long serialVersionUID = 1L;
        private String externalId; private String title; private String difficulty; private String knowledgePoints;
        private String description; private String inputDescription; private String outputDescription; private String constraintsText;
        private String sampleExplanation; private String notesText; private String starterCode; private String referenceCode;
        private String noInput; private String isPublic; private Double timeLimitSeconds; private Integer memoryLimitKb;
        public String getExternalId(){return externalId;} public void setExternalId(String v){externalId=v;}
        public String getTitle(){return title;} public void setTitle(String v){title=v;}
        public String getDifficulty(){return difficulty;} public void setDifficulty(String v){difficulty=v;}
        public String getKnowledgePoints(){return knowledgePoints;} public void setKnowledgePoints(String v){knowledgePoints=v;}
        public String getDescription(){return description;} public void setDescription(String v){description=v;}
        public String getInputDescription(){return inputDescription;} public void setInputDescription(String v){inputDescription=v;}
        public String getOutputDescription(){return outputDescription;} public void setOutputDescription(String v){outputDescription=v;}
        public String getConstraintsText(){return constraintsText;} public void setConstraintsText(String v){constraintsText=v;}
        public String getSampleExplanation(){return sampleExplanation;} public void setSampleExplanation(String v){sampleExplanation=v;}
        public String getNotesText(){return notesText;} public void setNotesText(String v){notesText=v;}
        public String getStarterCode(){return starterCode;} public void setStarterCode(String v){starterCode=v;}
        public String getReferenceCode(){return referenceCode;} public void setReferenceCode(String v){referenceCode=v;}
        public String getNoInput(){return noInput;} public void setNoInput(String v){noInput=v;}
        public String getIsPublic(){return isPublic;} public void setIsPublic(String v){isPublic=v;}
        public Double getTimeLimitSeconds(){return timeLimitSeconds;} public void setTimeLimitSeconds(Double v){timeLimitSeconds=v;}
        public Integer getMemoryLimitKb(){return memoryLimitKb;} public void setMemoryLimitKb(Integer v){memoryLimitKb=v;}
    }

    public static class TestCaseRow implements Serializable {
        private static final long serialVersionUID = 1L;
        private String externalId; private String caseName; private String inputText; private String expectedOutput;
        private String isPublic; private Double scoreWeight; private Integer orderNum;
        public String getExternalId(){return externalId;} public void setExternalId(String v){externalId=v;}
        public String getCaseName(){return caseName;} public void setCaseName(String v){caseName=v;}
        public String getInputText(){return inputText;} public void setInputText(String v){inputText=v;}
        public String getExpectedOutput(){return expectedOutput;} public void setExpectedOutput(String v){expectedOutput=v;}
        public String getIsPublic(){return isPublic;} public void setIsPublic(String v){isPublic=v;}
        public Double getScoreWeight(){return scoreWeight;} public void setScoreWeight(Double v){scoreWeight=v;}
        public Integer getOrderNum(){return orderNum;} public void setOrderNum(Integer v){orderNum=v;}
    }
}
