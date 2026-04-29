package cci.pdfbuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

import cci.model.owncert.Product;
import cci.web.controller.owncert.ViewOwnCertificateJSPHelper;

import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PRStream;

@Service
public class PDFUtils {
	private static final Logger LOG=Logger.getLogger(PDFUtils.class);
    private final int x_pos = 468;
    private final int y_pos = 765;
    
	/* ----------------------------------
	 * 	 PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
	 *	 FileOutputStream fos = new FileOutputStream(dest);
     *   fos.write(output.toByteArray());
     *   fos.close();
	 * ---------------------------------- */
	public ByteArrayOutputStream mergePdf(String src, String back1, String back2, List<String> numbers, int checksize)
			throws IOException, DocumentException {
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		boolean isImage = checkImagesInFile(src, checksize);
		
		if (isImage) {
			PdfReader reader = new PdfReader(src);
			PdfStamper stamper = new PdfStamper(reader, output);
			stamper.close();
			reader.close();
		} else {
			PdfReader reader = new PdfReader(src);
			PdfStamper stamper = new PdfStamper(reader, output);
			invert(stamper);
			PdfReader s_back_1 = new PdfReader(back1);
			PdfReader s_back_2 = new PdfReader(back2);

			PdfImportedPage back_1 = stamper.getImportedPage(s_back_1, 1);
			PdfImportedPage back_2 = stamper.getImportedPage(s_back_2, 1);

			int n = reader.getNumberOfPages();
			PdfContentByte canvas;

			for (int i = 1; i <= n; i++) {
				canvas = stamper.getOverContent(i);
				canvas.addTemplate(i == 1 ? back_1 : back_2, 0, 0);

				if (numbers != null && numbers.size() >= i && !numbers.get(i - 1).isEmpty()) {
					Font ft = new Font();
					ft.setColor(BaseColor.RED);
					ft.setSize(14);
					ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, new Phrase(numbers.get(i - 1), ft), x_pos,
							y_pos, 0);
				}
			}
			stamper.close();
			s_back_1.close();
			s_back_2.close();
			reader.close();
		}
		return output;
	}
	
	/* -------------------------------------------------------------------------------
	 * 
	 *  Create text PDF copy of own certificate 
	 * -------------------------------------------------------------------------------
	 */
	public ByteArrayOutputStream createPDF(ViewOwnCertificateJSPHelper viewcert) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Document doc = new Document();
		
		try {
			BaseFont baseFont = BaseFont.createFont("C:\\Windows\\Fonts\\arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED); 
			Font font = new Font(baseFont, 12, Font.NORMAL, BaseColor.BLACK);
			Font fontTitle = new Font(baseFont, 16, Font.NORMAL, BaseColor.BLACK);
			
        	PdfWriter writer = PdfWriter.getInstance(doc, output);
            doc.open();
            
            doc.add(makePRG("БЕЛОРУССКАЯ ТОРГОВО-ПРОМЫШЛЕННАЯ ПАЛАТА", font, Element.ALIGN_CENTER));
            doc.add(makePRG(viewcert.getCert().getBeltpp().getName(), font, Element.ALIGN_CENTER));
            doc.add(makePRG(viewcert.getCert().getBeltpp().getAddress() + "  " + viewcert.getCert().getBeltpp().getPhone(), font, Element.ALIGN_CENTER));
            doc.add(makePRG("СЕРТИФИКАТ № " + viewcert.getCert().getNumber(), fontTitle,Element.ALIGN_CENTER));
            doc.add(makePRG(viewcert.getCerttype(), font, Element.ALIGN_CENTER));
            doc.add(makePRG("1. Производитель: " + viewcert.getCustomer(), font, Element.ALIGN_LEFT));
            doc.add(makePRG(viewcert.getTitlebrancheslist() + ": ",  font, Element.ALIGN_LEFT));
            doc.add(makePRG(viewcert.getCert().getBranchlist(), font, Element.ALIGN_LEFT));
            doc.add(makePRG(viewcert.getTitleunp() + viewcert.getCert().getCustomerunp() , font, Element.ALIGN_LEFT));
            doc.add(makePRG(viewcert.getPlaceproduction(), font, Element.ALIGN_LEFT));
            doc.add(makePRG(viewcert.getTitleproductslist(), font, Element.ALIGN_LEFT));
            doc.add(makePRG(" ", font, Element.ALIGN_LEFT));
            
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(95);
            float[] columnWidths = {2f, 50f, 10f}; 
            table.setWidths(columnWidths);
            table.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);
            table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            
            for (Product product: viewcert.getCert().getProducts()) {
            	PdfPCell cell = new PdfPCell(makePRG(product.getNumber(), font, Element.ALIGN_LEFT));
            	cell.setBorder(Rectangle.NO_BORDER);
            	table.addCell(cell);
            	cell = new PdfPCell(makePRG(product.getName(), font, Element.ALIGN_LEFT));
            	cell.setBorder(Rectangle.NO_BORDER);
            	table.addCell(cell);
            	cell = new PdfPCell(makePRG(product.getCode(), font, Element.ALIGN_LEFT));
            	cell.setBorder(Rectangle.NO_BORDER);
            	table.addCell(cell);
            }
            doc.add(table);
            doc.add(makePRG(viewcert.getValiddates(), font, Element.ALIGN_LEFT));
            doc.add(makePRG(viewcert.getConfirmation(), font, Element.ALIGN_LEFT));
            doc.add(makePRG(viewcert.getCert().getSignerjob() + " " + viewcert.getCert().getSigner() + " " + viewcert.getCert().getDatecert(), font, Element.ALIGN_LEFT));
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            doc.close();
        }

		return output;
		
	}
	
	private Paragraph makePRG(String text, Font font, int elmnt) {
        Paragraph prg = new Paragraph(text, font);
        prg.setAlignment(elmnt);
        return prg;
	}
	
    /* -----------------------------------------
     * 
     * 
     *-------------------------------------------*/
	void invert(PdfStamper stamper) {
		for (int i = stamper.getReader().getNumberOfPages(); i > 0; i--) {
			reinvertPage(stamper, i);
		}
	}
	/* -----------------------------------------
     * 
     * 
     *-------------------------------------------*/
	void reinvertPage(PdfStamper stamper, int page) {
		Rectangle rect = stamper.getReader().getPageSize(page);
		PdfContentByte cb = stamper.getOverContent(page);
		PdfGState gs = new PdfGState();
		gs.setFillOpacity(1f);
		gs.setBlendMode(PdfGState.BM_DARKEN);
		cb.setGState(gs);
		cb.rectangle(rect.getLeft(), rect.getBottom(), rect.getWidth(), rect.getHeight());
	}
	
	private boolean checkImagesInFile(String filename, int checksize)  {
		PdfReader preader = null;
				
		try {
			preader = new PdfReader(filename); 
			PdfObject po; 
			PRStream pst;
			PdfObject type;
			int n = preader.getXrefSize(); //number of objects in pdf document
			
			for(int i=0;i<n;i++) {
			   po = preader.getPdfObject(i); 
			   if( po==null || !po.isStream()) continue;
			   pst = (PRStream) po;
			   type = pst.get(PdfName.SUBTYPE); 
			   // LOG.info(po.toString() + " | " + type.toString() + " | " + pst.getLength());
			   
			   if (type!=null && type.toString().equals(PdfName.IMAGE.toString()) && pst.getLength() > checksize ) {
				 LOG.info("Image in pdf file is found ! " + pst.getLength());
	             return true;  		      	   
			   } else {
				 LOG.info("Image in pdf file is NOT found ! " + pst.getLength());  
			   }
			}
		} catch (Exception ex) {
		  LOG.error(ex.getLocalizedMessage());	
		} finally {
		  if (preader != null) preader.close();
		}
		
		if (preader != null) preader.close();
		return false;
	}
}
