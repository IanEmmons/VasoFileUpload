package org.virginiaso.file_upload;

public class UserSubmission {
	private String division;
	private int teamNumber;
	private String schoolName;
	private String teamName;
	private String studentNames;
	private String notes;
	private String helicopterMode;

	public String getDivision() {
		return division;
	}

	public void setDivision(String division) {
		this.division = division;
	}

	public int getTeamNumber() {
		return teamNumber;
	}

	public void setTeamNumber(int teamNumber) {
		this.teamNumber = teamNumber;
	}

	public String getSchoolName() {
		return schoolName;
	}

	public void setSchoolName(String schoolName) {
		this.schoolName = schoolName;
	}

	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public String getStudentNames() {
		return studentNames;
	}

	public void setStudentNames(String studentNames) {
		this.studentNames = studentNames;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getHelicopterMode() {
		return helicopterMode;
	}

	public void setHelicopterMode(String helicopterMode) {
		this.helicopterMode = helicopterMode;
	}
}