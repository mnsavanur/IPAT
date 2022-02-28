package com.lti.TestUtil;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.multipdf.LayerUtility;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;

import com.aspose.pdf.Document;
import com.aspose.pdf.Page;
import com.aspose.pdf.PageCollection;
import com.aspose.pdf.SaveFormat;
import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;
import com.lti.base.Config;
import com.lti.base.Init;
import com.lti.webDriver.Assertions;


import de.redsix.pdfcompare.CompareResultImpl;
import de.redsix.pdfcompare.PageArea;
import de.redsix.pdfcompare.PdfComparator;
import de.redsix.pdfcompare.env.SimpleEnvironment;

/*
 * Date - 5/27/2021
 * Author - Sheetal Jadhav
 * Description - PDF Compare
*/

public class ExternalFileHandling {
static Logger log = Logger.getLogger(ExternalFileHandling.class.getName());
public static void clearDownloadFolder()
{
	File downloadDir=new File(Config.downloadPath);
	File[] files=downloadDir.listFiles();
	try {
		for(File file:files) {
			String fileExtn = FilenameUtils.getExtension(file.getName());
			if(!fileExtn.isEmpty()){
				file.delete();
			}
		}
	}catch(Exception e) {
		log.info("Issue while clearing Downloads folders");
	}
}
public static String[] comparePDFNew(String expectedFile, String actualFile, String[] excludeString, String resultPath) {
	// TODO Auto-generated method stub

	String[] resultArr=new String[3];
	String resultString="";
	try {
		
		File expFile=new File(expectedFile);
		if(!expFile.exists()) {
			resultArr[0]="Fail";
			resultArr[1]="Expected PDF file missing.";
			resultArr[2]="Expected PDF file missing.";
			return resultArr;
		}
		
		File actFile=new File(actualFile);
		if(!actFile.exists()) {
			resultArr[0]="Fail";
			resultArr[1]="Actual PDF file missing.";
			resultArr[2]="Actual PDF file missing.";
			return resultArr;
		}
		
		File resPath = new File(resultPath);
		if(!resPath.exists()) {
			resPath.mkdirs();
		}
		
		System.gc();
		PDDocument doc = PDDocument.load(new File(expectedFile));
		int expectedFilePageCount = doc.getNumberOfPages();
		doc = PDDocument.load(new File(actualFile));
		int actualFilePageCount = doc.getNumberOfPages();
		
		
		PdfComparator pdfCompObj=new PdfComparator(actualFile,expectedFile);
		if(excludeString!=null) {
			//pdfUtil.excludeText(excludeString);
		}
		pdfCompObj.withEnvironment(new SimpleEnvironment().setActualColor(Color.RED).setExpectedColor(Color.WHITE));
		CompareResultImpl resultFinal=pdfCompObj.compare();
		String resultFileNameWithPathWithoutExtn=resultPath+"/"+expFile.getName().replace(".pdf", "_result1");
		
		
		boolean filesEqual = resultFinal.writeTo(resultFileNameWithPathWithoutExtn);
		int countOfDiffPages=0;
		
		//int countOfDiffPages=resultFinal.getNumberOfPages();
		Collection<PageArea> pageArea=resultFinal.getDifferences();
		Collection<Integer> diffPages=resultFinal.getPagesWithDifferences();
		countOfDiffPages=diffPages.size();
		String resultFileNameWithPath=resultFileNameWithPathWithoutExtn+".pdf";
		
		
		System.gc();
		PdfComparator pdfCompObj2=new PdfComparator(actualFile,expectedFile);
		pdfCompObj2.withEnvironment(new SimpleEnvironment().setActualColor(Color.WHITE).setExpectedColor(Color.RED));
		CompareResultImpl resultFinal2=pdfCompObj2.compare();
		String resultFileNameWithPathWithoutExtn2=resultPath+"/"+expFile.getName().replace(".pdf", "_result2");
		resultFinal2.writeTo(resultFileNameWithPathWithoutExtn2);
		String resultFileNameWithPath2=resultFileNameWithPathWithoutExtn2+".pdf";
		System.gc();
		String compareResultPath=resultFileNameWithPath.replace("_result1.pdf", "_CompareResult.pdf");
		generateSideBySidePDF(resultFileNameWithPath,resultFileNameWithPath2,compareResultPath);
		System.gc();
		String pageCountString="Expected File Page Count="+expectedFilePageCount+", Actual File Page Count="+actualFilePageCount+".";
		
		if(!filesEqual) {
			resultArr[0]="Fail";
			resultArr[1]= "PDF comparison failed.\n"+ pageCountString +"\nDifference found in "+ countOfDiffPages +" pages. Find the mismatch result at : '"+ compareResultPath +"'";
			//resultArr[2]= "PDF comparison failed. Difference found in "+ countOfDiffPages +" pages. Find the mismatch result at : <a href='"+ resultFileNameWithPath+"'>Click Here</a>";
			resultArr[2]= "PDF comparison failed.<br>"+ pageCountString + "<br>Difference found in "+ countOfDiffPages +" pages. Find the mismatch result at : <a href='"+ compareResultPath+"'>Click Here</a>";
		}else {
			pageCountString= "Expected and Actual File Page Count="+pageCountString;
			resultArr[0]="Pass";
			resultArr[1]= "PDF comparison successful. No difference found." + pageCountString;
			resultArr[2]= "PDF comparison successful. No difference found."+ pageCountString;
		}
	
	}catch(Exception e)	{
		e.printStackTrace();
	}
	finally {
		return resultArr;
	}
}

public static String[] divideAndComparePDF(String expectedFile, String actualFile, String[] excludeString, String resultPath) {
	// TODO Auto-generated method stub

	String[] resultArr=new String[3];
	String resultString="";
	try {
		
		File expFile=new File(expectedFile);
		if(!expFile.exists()) {
			resultArr[0]="Fail";
			resultArr[1]="Expected PDF file missing.";
			resultArr[2]="Expected PDF file missing.";
			return resultArr;
		}
		
		File actFile=new File(actualFile);
		if(!actFile.exists()) {
			resultArr[0]="Fail";
			resultArr[1]="Actual PDF file missing.";
			resultArr[2]="Actual PDF file missing.";
			return resultArr;
		}
		
		File resPath = new File(resultPath);
		if(!resPath.exists()) {
			resPath.mkdirs();
		}
		String finalResultFile=resultPath+"/"+expFile.getName().replace(".pdf", "_CompareResult.pdf");
		
		PDDocument doc = PDDocument.load(new File(expectedFile));
		int expectedFilePageCount = doc.getNumberOfPages();
		doc = PDDocument.load(new File(actualFile));
		int actualFilePageCount = doc.getNumberOfPages();
		doc.close();
		
		int maxPages=(expectedFilePageCount>=actualFilePageCount)?expectedFilePageCount:actualFilePageCount;
		int minPages=(expectedFilePageCount>=actualFilePageCount)?actualFilePageCount:expectedFilePageCount;
		int pageCtr=1, i=1;
		
		//create Temp folder for storing temp part PDFs
		String tempFolderStr=Config.downloadPath+"\\"+"Temp";
		File tempFolder=new File(tempFolderStr);
		if(!tempFolder.exists()) {
			tempFolder.mkdirs();
		}
		int ctr=1;
		Boolean filesEqual=true;
		int countOfDiffPages=0;
		while(pageCtr <= maxPages) {
			int nextPageCtr;
			int startPage=pageCtr;
			int endPage=pageCtr+9;
			int actualStartPage=0, actualEndPage=0;
			int expectedStartPage=0, expectedEndPage=0;
			
			//extract 10 or remaining pages from actual PDF
			if(startPage<=actualFilePageCount)
				actualStartPage=startPage;
			else
				actualStartPage=-1;
			
			if(endPage<=actualFilePageCount)
				actualEndPage=endPage;
			else if(actualFilePageCount>=startPage && actualFilePageCount<=endPage) {
				actualEndPage=actualFilePageCount;
				//nextPageCtr=actualFilePageCount;
			}
			else
				actualEndPage=-1;
			
			//extract 10 or remaining pages from expected PDF
			if(startPage<=expectedFilePageCount)
				expectedStartPage=startPage;
			else 
				expectedStartPage=-1;
			
			if(endPage<=expectedFilePageCount)
				expectedEndPage=endPage;
			else if(expectedFilePageCount>=startPage && expectedFilePageCount<=endPage) {
				expectedEndPage=expectedFilePageCount;
				//nextPageCtr=expectedFilePageCount;
			}
			else
				expectedEndPage=-1;
			
			if(expectedEndPage != actualEndPage && !(expectedEndPage==-1 || actualEndPage==-1))
			{
				int curEndPage=0;
				if(expectedEndPage<actualEndPage) {
					actualEndPage=expectedEndPage;
					curEndPage=expectedEndPage;
				}else {
					expectedEndPage=actualEndPage;
					curEndPage=actualEndPage;
				}
				nextPageCtr=pageCtr+curEndPage%pageCtr+1;
			}else {
				nextPageCtr=pageCtr+10;
			}
			
			String actTempFileName=tempFolder+"\\act_"+actualStartPage+"to"+actualEndPage+".pdf";
			getPartPDF(actualFile, actualStartPage, actualEndPage, actTempFileName);
			
			String expTempFileName=tempFolder+"\\exp_"+expectedStartPage+"to"+expectedEndPage+".pdf";
			getPartPDF(expectedFile, expectedStartPage, expectedEndPage,expTempFileName);
			Thread.sleep(500);
			String resultTempFileName=tempFolderStr+"/"+expFile.getName().replace(".pdf", "_CompareResult"+"_"+ctr);
			int curCountOfDiffPages=comparePDF(new File(expTempFileName), new File(actTempFileName), excludeString, tempFolderStr, resultTempFileName);
			
			if(expectedStartPage!=-1 && expectedEndPage!=-1 && actualStartPage !=-1 && actualEndPage != -1) {
				countOfDiffPages= countOfDiffPages + curCountOfDiffPages;
			}
			
			if(filesEqual==true && countOfDiffPages>0) {
				filesEqual=false;
			}
			//merge compare files
			PDFMergerUtility merger=new PDFMergerUtility();
			merger.setDestinationFileName(finalResultFile);
			if(ctr>1)
				merger.addSource(new File(finalResultFile));
			merger.addSource(new File(resultTempFileName+".pdf"));
			merger.mergeDocuments(null);
			
			Init.clearFolder(tempFolder);
			
			//Init.cleanTempDir();
			System.gc();
			pageCtr=nextPageCtr;
			ctr++;
		}
		if(tempFolder.exists()) {
			tempFolder.delete();
		}
		String pageCountString="Expected File Page Count="+expectedFilePageCount+", Actual File Page Count="+actualFilePageCount+".";
		
		if(!filesEqual) {
			resultArr[0]="Fail";
			resultArr[1]= "PDF comparison failed.\n"+ pageCountString +"\nDifference found in "+ countOfDiffPages +" pages. Find the mismatch result at : '"+ finalResultFile +"'";
			//resultArr[2]= "PDF comparison failed. Difference found in "+ countOfDiffPages +" pages. Find the mismatch result at : <a href='"+ resultFileNameWithPath+"'>Click Here</a>";
			resultArr[2]= "PDF comparison failed.<br>"+ pageCountString + "<br>Difference found in "+ countOfDiffPages +" pages. Find the mismatch result at : <a href='"+ finalResultFile+"'>Click Here</a>";
		}else {
			pageCountString= "Expected and Actual File Page Count="+pageCountString;
			resultArr[0]="Pass";
			resultArr[1]= "PDF comparison successful. No difference found." + pageCountString;
			resultArr[2]= "PDF comparison successful. No difference found."+ pageCountString;
		}
	
	}catch(Exception e)	{
		e.printStackTrace();
	}
	finally {
		return resultArr;
	}
}
//not useful. Only 4 pages can be extracted
public static void getPartPDF2(String pdfFileName, int startPage, int endPage, String resultDocPath) throws Exception{
	if(startPage>endPage) {
		Document newDocument = new Document();
		newDocument.save();
		return;
	}
	
	Document pdfDocument = new Document(pdfFileName);
	//PageCollection  pages = pdfDocument.getPages();
	List<Page> listOfPages = new ArrayList<Page>();
	int curPage=startPage;
	log.info("tbd");
	for (Page pdfPage : pdfDocument.getPages()) {
		if(pdfPage.getNumber()>=startPage && pdfPage.getNumber()<=endPage) {
			while(endPage>=curPage) {
				listOfPages.add(pdfPage);
				curPage++;
			}
		}
	}
	Document newDocument = new Document();
	newDocument.getPages().add(listOfPages);
	newDocument.save(resultDocPath);
	newDocument.close();
}
public static void getPartPDF(String pdfFileName, int startPage, int endPage, String resultDocPath) throws Exception{
	if(startPage ==-1 || endPage ==-1) {
		PDDocument newDocument = new PDDocument();
		newDocument.save(resultDocPath);
		return;
	}
	File file = new File(pdfFileName);
    PDDocument document = PDDocument.load(file); 
    PDDocument newDocument = new PDDocument();
	for(int pIterator=1;pIterator<=document.getNumberOfPages();pIterator++) {
		if(pIterator>=startPage && pIterator<=endPage)
			newDocument.addPage(document.getPage(pIterator-1));
	}
    newDocument.save(resultDocPath);
    newDocument.close();
}
public static int comparePDF(File expectedFile, File actualFile, String[] excludeString, String resultPath, String resultFileName)throws Exception {
	
	if(excludeString!=null) {
		//pdfUtil.excludeText(excludeString);
	}
	int countOfDiffPages=0;
	
	//highlight one side
	PdfComparator pdfCompObj=new PdfComparator(actualFile,expectedFile);
	pdfCompObj.withEnvironment(new SimpleEnvironment().setActualColor(Color.RED).setExpectedColor(Color.WHITE));
	CompareResultImpl resultFinal=pdfCompObj.compare();
	String resultFileNameWithPathWithoutExtn=resultPath+"/"+expectedFile.getName().replace(".pdf", "_result1");
	boolean filesEqual = resultFinal.writeTo(resultFileNameWithPathWithoutExtn);
	Collection<PageArea> pageArea=resultFinal.getDifferences();
	Collection<Integer> diffPages=resultFinal.getPagesWithDifferences();
	
	countOfDiffPages=diffPages.size();
	String resultFileNameWithPath=resultFileNameWithPathWithoutExtn+".pdf";
	
	//highlight another side
	PdfComparator pdfCompObj2=new PdfComparator(actualFile,expectedFile);
	pdfCompObj2.withEnvironment(new SimpleEnvironment().setActualColor(Color.WHITE).setExpectedColor(Color.RED));
	CompareResultImpl resultFinal2=pdfCompObj2.compare();
	String resultFileNameWithPathWithoutExtn2=resultPath+"/"+expectedFile.getName().replace(".pdf", "_result2");
	resultFinal2.writeTo(resultFileNameWithPathWithoutExtn2);
	
	String resultFileNameWithPath2=resultFileNameWithPathWithoutExtn2+".pdf";
	
	//String compareResultPath=resultFileNameWithPath.replace("_result1.pdf", "_CompareResult.pdf");
	generateSideBySidePDF(resultFileNameWithPath,resultFileNameWithPath2,resultFileName+".pdf");
	return countOfDiffPages;
}


public static void generateSideBySidePDF(String FILE1_PATH, String FILE2_PATH, String OUTFILE_PATH) {
    File pdf1File = new File(FILE1_PATH);
    File pdf2File = new File(FILE2_PATH);
    File outPdfFile = new File(OUTFILE_PATH);
    PDDocument pdf1 = null;
    PDDocument pdf2 = null;
    PDDocument outPdf = null;
    try {

        pdf1 = PDDocument.load(pdf1File);
        pdf2 = PDDocument.load(pdf2File);

        outPdf = new PDDocument();
        for(int pageNum = 0; pageNum < pdf1.getNumberOfPages(); pageNum++) {
            // Create output PDF frame
            PDRectangle pdf1Frame = pdf1.getPage(pageNum).getCropBox();
            PDRectangle pdf2Frame = pdf2.getPage(pageNum).getCropBox();
            PDRectangle outPdfFrame = new PDRectangle(pdf1Frame.getWidth()+pdf2Frame.getWidth(), Math.max(pdf1Frame.getHeight(), pdf2Frame.getHeight()));

            // Create output page with calculated frame and add it to the document
            COSDictionary dict = new COSDictionary();
            dict.setItem(COSName.TYPE, COSName.PAGE);
            dict.setItem(COSName.MEDIA_BOX, outPdfFrame);
            dict.setItem(COSName.CROP_BOX, outPdfFrame);
            dict.setItem(COSName.ART_BOX, outPdfFrame);
            PDPage outPdfPage = new PDPage(dict);
            outPdf.addPage(outPdfPage);

            // Source PDF pages has to be imported as form XObjects to be able to insert them at a specific point in the output page
            LayerUtility layerUtility = new LayerUtility(outPdf);
            PDFormXObject formPdf1 = layerUtility.importPageAsForm(pdf1, pageNum);
            PDFormXObject formPdf2 = layerUtility.importPageAsForm(pdf2, pageNum);

            // Add form objects to output page
            AffineTransform afLeft = new AffineTransform();
            layerUtility.appendFormAsLayer(outPdfPage, formPdf1, afLeft, "left" + pageNum);
            AffineTransform afRight = AffineTransform.getTranslateInstance(pdf1Frame.getWidth(), 0.0);
            layerUtility.appendFormAsLayer(outPdfPage, formPdf2, afRight, "right" + pageNum);
        }
        outPdf.save(outPdfFile);
        outPdf.close();

    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        try {
            if (pdf1 != null) pdf1.close();
            if (pdf2 != null) pdf2.close();
            if (outPdf != null) outPdf.close();
            pdf1.close();
            pdf2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
public static void CompareXML() {
	
}
public static void convertDocmToPdf(String docmFile, String pdfFile) {
	IConverter converter=null;
	try {
		File wordFile = new File(docmFile); 
		File target = new File(pdfFile);
		
		File tempFolder=new File(Config.downloadPath+"\\"+"tempDocm");
		if(!tempFolder.exists())
			tempFolder.mkdirs();
		converter = LocalConverter.builder().baseFolder(new File(tempFolder.getAbsolutePath()))
		            .workerPool(20, 25, 2, TimeUnit.SECONDS)
		            .processTimeout(60, TimeUnit.SECONDS)
		            .build();		
		//converter.convert(wordFile).as(DocumentType.MS_WORD).to(target).as(DocumentType.PDF).schedule();
		converter.convert(wordFile).as(DocumentType.MS_WORD).to(target).as(DocumentType.PDF).schedule();
	    converter.shutDown();
	    if(tempFolder.exists()) {
	    	Init.clearFolder(tempFolder);
	    	tempFolder.delete();
	    }
	}catch(Exception e) {
		converter.shutDown();
		log.info("Issue while converting docm to pdf." + e.getMessage());
	}
}
}

