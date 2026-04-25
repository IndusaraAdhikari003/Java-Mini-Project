package com.example.javaminiproject.util;

public class GradeCalculator {

    // UGC Commission Circular No. 12-2024 grading
    public static String getGrade(double finalMark) {
        if (finalMark >= 85) return "A+";
        if (finalMark >= 75) return "A";
        if (finalMark >= 70) return "A-";
        if (finalMark >= 65) return "B+";
        if (finalMark >= 60) return "B";
        if (finalMark >= 55) return "B-";
        if (finalMark >= 50) return "C+";
        if (finalMark >= 45) return "C";
        if (finalMark >= 40) return "C-";
        if (finalMark >= 35) return "D+";
        if (finalMark >= 30) return "D";
        return "E";
    }

    public static double getGradePoint(String grade) {
        return switch (grade) {
            case "A+"  -> 4.0;
            case "A"   -> 4.0;
            case "A-"  -> 3.7;
            case "B+"  -> 3.3;
            case "B"   -> 3.0;
            case "B-"  -> 2.7;
            case "C+"  -> 2.3;
            case "C"   -> 2.0;
            case "C-"  -> 1.7;
            case "D+"  -> 1.3;
            case "D"   -> 1.0;
            default    -> 0.0; // E
        };
    }

    // CA eligibility: CA average must be >= 40%
    public static boolean isCAEligible(double caAverage) {
        return caAverage >= 40.0;
    }

    // Attendance eligibility: >= 80%
    public static boolean isAttendanceEligible(double attendancePercent) {
        return attendancePercent >= 80.0;
    }

    // SGPA for one semester
    public static double calculateSGPA(double[] gradePoints, int[] credits) {
        double totalPoints = 0;
        int    totalCredits = 0;
        for (int i = 0; i < gradePoints.length; i++) {
            totalPoints  += gradePoints[i] * credits[i];
            totalCredits += credits[i];
        }
        return totalCredits == 0 ? 0.0 : totalPoints / totalCredits;
    }

}