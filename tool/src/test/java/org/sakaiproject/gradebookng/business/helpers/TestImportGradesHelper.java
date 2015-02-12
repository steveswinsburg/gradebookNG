package org.sakaiproject.gradebookng.business.helpers;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.sakaiproject.gradebookng.business.model.*;
import org.sakaiproject.gradebookng.tool.model.GradeInfo;
import org.sakaiproject.gradebookng.tool.model.StudentGradeInfo;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.user.api.User;

import java.io.InputStream;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chmaurer on 1/24/15.
 */
public class TestImportGradesHelper {

    @Test
    public void testCsvImport() throws Exception {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("grades_import.csv");
        ImportedGradeWrapper importedGradeWrapper = ImportGradesHelper.parseCsv(is);
        is.close();

        testImport(importedGradeWrapper);
    }

    @Test
    public void testXlsImport() throws Exception {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("grades_import.xls");
        ImportedGradeWrapper importedGradeWrapper = ImportGradesHelper.parseXls(is);
        is.close();

        testImport(importedGradeWrapper);
    }

    private void testImport(ImportedGradeWrapper importedGradeWrapper) throws Exception {
        Assert.assertNotNull(importedGradeWrapper);

        List<ImportedGrade> importedGrades = importedGradeWrapper.getImportedGrades();

        Assert.assertNotNull(importedGrades);
        Assert.assertEquals("unexpected list size", 2, importedGrades.size());

        Assert.assertNotNull(importedGrades.get(0).getGradeItemMap());
        Assert.assertEquals(2, importedGrades.get(0).getGradeItemMap().size());

        ImportedGradeItem item11 = importedGrades.get(0).getGradeItemMap().get("a1");
        Assert.assertEquals("comments don't match", "graded", item11.getGradeItemComment());
        Assert.assertEquals("scores don't match", "7", item11.getGradeItemScore());

        ImportedGradeItem item12 = importedGrades.get(0).getGradeItemMap().get("food");
        Assert.assertEquals("comments don't match", "null", item12.getGradeItemComment());
        Assert.assertEquals("scores don't match", "null", item12.getGradeItemScore());

        ImportedGradeItem item21 = importedGrades.get(1).getGradeItemMap().get("a1");
        Assert.assertEquals("comments don't match", "interesting work", item21.getGradeItemComment());
        Assert.assertEquals("scores don't match", "3", item21.getGradeItemScore());

        ImportedGradeItem item22 = importedGrades.get(1).getGradeItemMap().get("food");
        Assert.assertEquals("comments don't match", "I'm hungry", item22.getGradeItemComment());
        Assert.assertEquals("scores don't match", "42", item22.getGradeItemScore());
    }

    @Test
    public void testParseAssignmentHeader() throws Exception {
        String inputString = "The Assignment [10]";

        MessageFormat mf = new MessageFormat(ImportGradesHelper.ASSIGNMENT_HEADER_PATTERN);
        Object[] parsedObject = mf.parse(inputString);

        Assert.assertEquals("Parsed assignment name does not match", "The Assignment", parsedObject[0]);
        Assert.assertEquals("Parsed assignment points do not match", "10", parsedObject[1]);

    }

    @Test
    public void testParseAssignmentCommentHeader() throws Exception {
        String inputString = "*/ The Assignment Comments */";

        MessageFormat mf = new MessageFormat(ImportGradesHelper.ASSIGNMENT_HEADER_COMMENT_PATTERN);
        Object[] parsedObject = mf.parse(inputString);

        Assert.assertEquals("Parsed assignment name does not match", "The Assignment", parsedObject[0]);

    }

    @Test(expected = ParseException.class)
    public void testBadParseAssignmentCommentHeader() throws Exception {
        String inputString = "*/ The Assignment Comments */";

        MessageFormat mf = new MessageFormat(ImportGradesHelper.ASSIGNMENT_HEADER_PATTERN);
        mf.parse(inputString);

    }

    @Test
    public void testProcessImportedGrades() throws Exception {
        List<Assignment> assignments = mockAssignments();
        List<StudentGradeInfo> grades = mockStudentGrades();
        ImportedGradeWrapper importedGradeWrapper = mockImportedGrades();

        List<ProcessedGradeItem> processedGradeItems = ImportGradesHelper.processImportedGrades(importedGradeWrapper, assignments, grades);

        Assert.assertNotNull(processedGradeItems);

        Assert.assertEquals("wrong status", ProcessedGradeItem.STATUS_NA, processedGradeItems.get(0).getStatus());
        Assert.assertEquals("wrong status", ProcessedGradeItem.STATUS_NA, processedGradeItems.get(1).getStatus());
        Assert.assertEquals("wrong status", ProcessedGradeItem.STATUS_UPDATE, processedGradeItems.get(2).getStatus());
        Assert.assertEquals("wrong status", ProcessedGradeItem.STATUS_UPDATE, processedGradeItems.get(3).getStatus());
        Assert.assertEquals("wrong status", ProcessedGradeItem.STATUS_NEW, processedGradeItems.get(4).getStatus());
        Assert.assertEquals("wrong status", ProcessedGradeItem.STATUS_NEW, processedGradeItems.get(5).getStatus());

    }

    /**
     * Mock up some assignment data
     * @return List of mocked assignments
     */
    private List<Assignment> mockAssignments() {
        List<Assignment> assignments = new ArrayList<Assignment>();
        Assignment assignment1 = new Assignment();
        assignment1.setId(1L);
        assignment1.setName("Assignment 1");
        assignment1.setPoints(10.0);
        assignments.add(assignment1);

        Assignment assignment2 = new Assignment();
        assignment2.setId(2L);
        assignment2.setName("Assignment 2");
        assignment2.setPoints(100.0);
        assignments.add(assignment2);

        return assignments;
    }

    /**
     * Mock up some student grade data
     * @return
     */
    private List<StudentGradeInfo> mockStudentGrades() {
        List<StudentGradeInfo> grades = new ArrayList<StudentGradeInfo>();

        User user1 = Mockito.mock(User.class);
        Mockito.when(user1.getId()).thenReturn("user1");
        Mockito.when(user1.getEid()).thenReturn("user1");
        StudentGradeInfo studentGradeInfo1 = new StudentGradeInfo(user1);
        GradeDefinition gradeDefinition1 = new GradeDefinition();
        gradeDefinition1.setGrade("1");
        gradeDefinition1.setGradeComment("comment 1");
        studentGradeInfo1.addGrade(1L, new GradeInfo(gradeDefinition1));
        grades.add(studentGradeInfo1);

        StudentGradeInfo studentGradeInfo2 = new StudentGradeInfo(user1);
        GradeDefinition gradeDefinition2 = new GradeDefinition();
        gradeDefinition2.setGrade("2");
        gradeDefinition2.setGradeComment("comment 2");
        studentGradeInfo2.addGrade(2L, new GradeInfo(gradeDefinition2));
        grades.add(studentGradeInfo2);

        User user2 = Mockito.mock(User.class);
        Mockito.when(user2.getId()).thenReturn("user2");
        Mockito.when(user2.getEid()).thenReturn("user2");
        StudentGradeInfo studentGradeInfo3 = new StudentGradeInfo(user2);
        GradeDefinition gradeDefinition3 = new GradeDefinition();
        gradeDefinition3.setGrade("5");
        gradeDefinition3.setGradeComment("comment 12");
        studentGradeInfo3.addGrade(1L, new GradeInfo(gradeDefinition3));
        grades.add(studentGradeInfo3);

        StudentGradeInfo studentGradeInfo4 = new StudentGradeInfo(user2);
        GradeDefinition gradeDefinition4 = new GradeDefinition();
        gradeDefinition4.setGrade("6");
        gradeDefinition4.setGradeComment("comment 22");
        studentGradeInfo4.addGrade(2L, new GradeInfo(gradeDefinition4));
        grades.add(studentGradeInfo4);


        return grades;
    }

    private ImportedGradeWrapper mockImportedGrades() {
        ImportedGradeWrapper importedGradeWrapper = new ImportedGradeWrapper();
        List<ImportColumn> columns = new ArrayList<ImportColumn>();
        columns.add(new ImportColumn("Student ID", null, ImportColumn.TYPE_REGULAR));
        columns.add(new ImportColumn("Student Name", null, ImportColumn.TYPE_REGULAR));
        columns.add(new ImportColumn("Assignment 1", "10.0", ImportColumn.TYPE_ITEM_WITH_POINTS));
        columns.add(new ImportColumn("Assignment 1", "N/A", ImportColumn.TYPE_ITEM_WITH_COMMENTS));
        columns.add(new ImportColumn("Assignment 2", "10.0", ImportColumn.TYPE_ITEM_WITH_POINTS));
        columns.add(new ImportColumn("Assignment 2", "N/A", ImportColumn.TYPE_ITEM_WITH_COMMENTS));
        columns.add(new ImportColumn("Assignment 3", "100.0", ImportColumn.TYPE_ITEM_WITH_POINTS));
        columns.add(new ImportColumn("Assignment 3", "N/A", ImportColumn.TYPE_ITEM_WITH_COMMENTS));

        importedGradeWrapper.setColumns(columns);

        List<ImportedGrade> importedGrades = new ArrayList<ImportedGrade>();
        ImportedGrade importedGrade1 = new ImportedGrade();
        importedGrade1.setStudentId("user1");
        importedGrade1.setStudentName("User 1");
        Map<String, ImportedGradeItem> gradeMap1 = new HashMap<>();

        gradeMap1.put("Assignment 1", new ImportedGradeItem("Assignment 1", "comment 1", "1"));
        gradeMap1.put("Assignment 2", new ImportedGradeItem("Assignment 2", "comment 2", "2"));
        importedGrade1.setGradeItemMap(gradeMap1);
        importedGrades.add(importedGrade1);

        ImportedGrade importedGrade2 = new ImportedGrade();
        importedGrade2.setStudentId("user2");
        importedGrade2.setStudentName("User 2");
        Map<String, ImportedGradeItem> gradeMap2 = new HashMap<>();

        gradeMap2.put("Assignment 1", new ImportedGradeItem("Assignment 1", "comment 12", "5"));
        gradeMap2.put("Assignment 2", new ImportedGradeItem("Assignment 2", "comment 222", "3"));
        importedGrade2.setGradeItemMap(gradeMap2);
        importedGrades.add(importedGrade2);

        ImportedGrade importedGrade3 = new ImportedGrade();
        importedGrade3.setStudentId("user3");
        importedGrade3.setStudentName("User 3");
        Map<String, ImportedGradeItem> gradeMap3 = new HashMap<>();

//        gradeMap3.put("Assignment 1", new ImportedGradeItem("Assignment 1", "comment 13", "5"));
        gradeMap3.put("Assignment 2", new ImportedGradeItem("Assignment 2", "comment 23", "6"));
        gradeMap3.put("Assignment 3", new ImportedGradeItem("Assignment 3", "comment 233", "7"));
        importedGrade3.setGradeItemMap(gradeMap3);
        importedGrades.add(importedGrade3);

        importedGradeWrapper.setImportedGrades(importedGrades);

        return importedGradeWrapper;
    }
}