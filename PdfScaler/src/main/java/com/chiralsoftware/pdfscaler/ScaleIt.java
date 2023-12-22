package com.chiralsoftware.pdfscaler;

import com.lowagie.text.Document;
import static com.lowagie.text.PageSize.LETTER;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import static java.lang.System.err;
import java.util.logging.Logger;

public final class ScaleIt {
    private static final Logger LOG = Logger.getLogger(ScaleIt.class.getName());
    
    public static void main(String[] args) throws Exception {
        for(String s : args) {
            // we require the file to end with .pdf
            if(s == null) continue;
            if(! s.toLowerCase().endsWith(".pdf")) continue;
            processFile(s);
        }
    }
    
    private static boolean isLetterSize(File file) throws Exception  {
        if(file == null) throw new NullPointerException("File was null");
        final PdfReader pdfReader = new PdfReader(new FileInputStream(file));
        for(int i = 1; i <= pdfReader.getNumberOfPages(); i++) {
//            LOG.info("The page size of page " + i + " is: " + pdfReader.getPageSize(i));
            final Rectangle rectangle = pdfReader.getPageSize(i);
            if(rectangle.getHeight() < LETTER.getHeight() * 0.95) { pdfReader.close(); return false; }
            if(rectangle.getHeight() > LETTER.getHeight() * 1.05) { pdfReader.close(); return false; }

            if(rectangle.getWidth() < LETTER.getWidth() * 0.95)  { pdfReader.close(); return false; }
            if(rectangle.getWidth() > LETTER.getWidth() * 1.05)  { pdfReader.close(); return false; }
        }
        pdfReader.close();
        return true;
    }
    
    private static void processFile(String inputFileName) throws Exception {
        if(inputFileName == null) {
            LOG.warning("Input file name was null");
            return;
        }
//        LOG.info("Reading file: " + inputFileName);
        // make it move the old file to a backup file first and then do the transform in place
        // make it (optionally) take a scale param from input
        final File inputFile = new File(inputFileName);
        if(isLetterSize(inputFile)) {
            LOG.info("file: " + inputFile + " is letter sized, skipping");
            return;
        }
        if(! (inputFile.exists() && inputFile.canRead())) {
            err.println("FIle: " + inputFile + " doesn't exist or can't read");
            return;
        }
        final File backupFile = new File(inputFileName + ".bak");
        if(backupFile.exists()) {
            err.println("Backup file: " + backupFile + " already exists");
            return;
        }
        
        final boolean renameResult = inputFile.renameTo(backupFile);
        if(! renameResult) {
            err.println("Couldn't rename file: " + inputFile + " to: " + backupFile);
            return;
        }
        
        final PdfReader pdfReader = new PdfReader(new FileInputStream(backupFile));
        final Document document = new Document(LETTER);
        final PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(inputFile));
        document.open();
        final PdfContentByte pdfContentByte =  pdfWriter.getDirectContent();
        for(int i = 1; i <= pdfReader.getNumberOfPages(); i++) {
//            LOG.info("Processing page: " + i);
            document.newPage();
            final PdfImportedPage pdfImportedPage = pdfWriter.getImportedPage(pdfReader, i);
            final float scale = LETTER.getHeight() / pdfImportedPage.getHeight();
            pdfContentByte.addTemplate(pdfImportedPage, scale, 0,0, scale, 0,0);
        }
        document.close();

    }
    
}
