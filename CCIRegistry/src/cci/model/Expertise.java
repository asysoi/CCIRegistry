package cci.model;

public class Expertise {
	   
    private int id;
    private String actnumber;
    private String filename;
    private byte[] fileContent; // Колонка BLOB

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int i) { this.id = i; }

    public String getActnumber() { return actnumber; }
    public void setActnumber(String number) { this.actnumber = number; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public byte[] getFileContent() { return fileContent; }
    public void setFileContent(byte[] fileContent) { this.fileContent = fileContent; }
}
