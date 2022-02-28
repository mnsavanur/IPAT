package com.lti.TestUtil;

/*
 * Date - 5/27/2021
 * Author - Sheetal Jadhav
 * Description - Screen recording during execution
*/

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.monte.media.Format;
import org.monte.media.Registry;
import org.monte.screenrecorder.ScreenRecorder;

import com.aventstack.extentreports.Status;
import com.lti.base.Base;
import com.lti.controller.TestCase;
import com.lti.webDriver.Assertions;

import org.monte.media.FormatKeys.MediaType;
import org.monte.media.math.Rational;

import static org.monte.media.AudioFormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;

public class CustomScreenRecorder extends ScreenRecorder {
		public static ScreenRecorder screenRecorder;
		public String name;
		public String finalFileName="";
		static Logger log = Logger.getLogger(CustomScreenRecorder.class.getName());
		public CustomScreenRecorder(GraphicsConfiguration cfg, Rectangle captureArea, Format fileFormat,
				Format screenFormat, Format mouseFormat, Format audioFormat, File movieFolder, String name)
						throws IOException, AWTException {
			super(cfg, captureArea, fileFormat, screenFormat, mouseFormat, audioFormat, movieFolder);
			this.name = name;

		}

		@Override
		protected File createMovieFile(Format fileFormat) throws IOException {

			if (!movieFolder.exists()) {
				movieFolder.mkdirs();
			} else if (!movieFolder.isDirectory()) {
				throw new IOException("\"" + movieFolder + "\" is not a directory.");
			}
			SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMMyy_HH_mm_ss");
			Report.setScreenRecordingName(name + "-" + dateFormat.format(new Date()) + "." + Registry.getInstance().getExtension(fileFormat));
			return new File(movieFolder,Report.getScreenRecordingName());
		}

		public static void startRecording(String methodName) throws Exception {
			log.info("Recording started for "+ methodName);
			Report.setScreenRecordingName("");
			File file = new File("./TestExecutionReport/ScreenRecordings/");
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			int width = screenSize.width;
			int height = screenSize.height;

			Rectangle captureSize = new Rectangle(0, 0, width, height);

			GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().
					getDefaultScreenDevice()
					.getDefaultConfiguration();

			screenRecorder = new CustomScreenRecorder(gc, captureSize,
					new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_AVI),
					new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
							CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, (int)24, FrameRateKey,
							Rational.valueOf(15), QualityKey, 1.0f, KeyFrameIntervalKey, (int)(15 * 60)),
					new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, "black", FrameRateKey, Rational.valueOf(30)),
					null, file, methodName);
			
			screenRecorder.start();

		}

		public static void stopRecording() throws Exception {
			screenRecorder.stop();
			String filePath= System.getProperty("user.dir")+"/TestExecutionReport/ScreenRecordings/" +Report.getScreenRecordingName();
			
			String msg="Recording saved at :"+ filePath;
			log.info(msg);
			String msgForExt="<a href='"+ filePath +"'>Click here</a> to refer screen recording.";
			Base.test.log(Status.PASS, msgForExt);
			
			Report.setScreenRecordingName("");
		}

}
