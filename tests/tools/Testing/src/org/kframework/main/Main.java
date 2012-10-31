package org.kframework.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.kframework.execution.Execution;
import org.kframework.execution.Task;
import org.kframework.tests.Program;
import org.kframework.tests.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Main {

	public static void main(String[] args) {

		// a little bit hack-ish but it works until somebody complains
		if (System.getProperty("user.dir").contains("jenkins")) {

			// remove anything from previous build
			try {
				ProcessBuilder pb = new ProcessBuilder("rm", "-rf",
						Configuration.k);
				Process process = pb.start();
				int exit = process.waitFor();
				String out = Task.readString(process.getInputStream());
				String err = Task.readString(process.getErrorStream());
				System.out.println(out);
				System.out.println(err);
				System.out.println(exit);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// first copy the k-framework artifacts
			try {
				ProcessBuilder pb = new ProcessBuilder("cp", "-r",
						"/var/lib/jenkins/workspace/k-framework",
						Configuration.k);
				Process process = pb.start();
				int exit = process.waitFor();
				String out = Task.readString(process.getInputStream());
				String err = Task.readString(process.getErrorStream());
				System.out.println(out);
				System.out.println(err);
				System.out.println(exit);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// build K
			try {
				ProcessBuilder pb = new ProcessBuilder("ant");
				pb.directory(new File(Configuration.getHome()));
				Process process = pb.start();
				int exit = process.waitFor();
				String out = Task.readString(process.getInputStream());
				String err = Task.readString(process.getErrorStream());
				System.out.println(out);
				System.out.println(err);
				if (exit != 0)
					System.exit(exit);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		List<Test> alltests = new LinkedList<Test>();

		// load config
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			System.out.println("Buildfile: " + Configuration.getConfig());
			Document doc = dBuilder.parse(new File(Configuration.getConfig()));
			Element root = doc.getDocumentElement();

			NodeList test = root.getElementsByTagName("test");
			for (int i = 0; i < test.getLength(); i++)
				alltests.add(new Test((Element) test.item(i)));

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// compile definitions first
		System.out.print("Kompiling the language definitions...");
		Map<Test, Task> definitions = new HashMap<Test, Task>();
		for (Test test : alltests) {
			Task def = test.getDefinitionTask();
			definitions.put(test, def);
			Execution.execute(def);
		}
		Execution.finish();

		// report
		for (Entry<Test, Task> entry : definitions.entrySet()) {
			entry.getKey().reportCompilation(entry.getValue());
		}

		// console display
		String kompileStatus = "\n";
		for (Entry<Test, Task> entry : definitions.entrySet()) {
			if (!entry.getKey().compiled(entry.getValue()))
				kompileStatus += "FAIL: " + entry.getKey().getLanguage()
								.substring(Configuration.getHome().length())
						+ "\n";
		}
		if (kompileStatus.equals("\n"))
			kompileStatus = "\tSUCCESS";
		System.out.println(kompileStatus + "\n");

		// compile pdf definitions
		System.out.print("Generating PDF documentation...");
		Map<Test, Task> pdfDefinitions = new HashMap<Test, Task>();
		for (Test test : alltests) {
			// also compile pdf if set
			if (test.getPdf()) {
				Task pdfDef = test.getPdfDefinitionTask();
				pdfDefinitions.put(test, pdfDef);
				Execution.execute(pdfDef);
			}
		}

		Execution.finish();
		//report
		for (Entry<Test, Task> entry : pdfDefinitions.entrySet()) {
			entry.getKey().reportPdfCompilation(entry.getValue());
		}

		// console display
		String pdfKompileStatus = "\n";
		for (Entry<Test, Task> entry : pdfDefinitions.entrySet()) {
			if (!entry.getKey().compiledPdf(entry.getValue()))
				pdfKompileStatus += "FAIL: "
						+ entry.getKey().getLanguage()
								.substring(Configuration.getHome().length())
						+ "\n";
		}
		if(pdfKompileStatus.equals("\n"))
			pdfKompileStatus = "\t\tSUCCESS";
		System.out.println(pdfKompileStatus + "\n");
		
		
		// execute all programs (for which corresponding definitions are
		// compiled)
		for (Entry<Test, Task> dentry : definitions.entrySet()) {
			Test test = dentry.getKey();
			if (test.compiled(dentry.getValue())) {

				System.out.print("Running "
						+ test.getLanguage().substring(
								Configuration.getHome().length())
						+ " programs... ");
				
				// execute
				List<Program> pgms = test.getPrograms();
				Map<Program, Task> all = new HashMap<Program, Task>();
				for (Program p : pgms) {
					Task task = p.getTask();
					all.put(p, task);
					Execution.tpe.execute(task);
				}

				Execution.finish();

				// report
				for (Entry<Program, Task> entry : all.entrySet()) {
					entry.getKey().reportTest(entry.getValue());
				}

				// console
				String pgmOut = "\n";
				for (Entry<Program, Task> entry : all.entrySet()) {
					if (!entry.getKey().success(entry.getValue())) {
						pgmOut += "FAIL: " + entry.getKey()
												.getAbsolutePath()
												.substring(
														Configuration.getHome()
																.length())
										+ "\n";
					}
				}
				if (pgmOut.equals("\n"))
					pgmOut = "\tSUCCESS";
				System.out.println(pgmOut + "\n");
			}
		}

	}

	public static void copyFolder(File src, File dest) throws IOException {

		if (src.getName().startsWith("."))
			return;

		if (src.isDirectory()) {

			// if directory not exists, create it
			if (!dest.exists()) {
				dest.mkdir();
			}

			// list all the directory contents
			String files[] = src.list();

			for (String file : files) {
				// construct the src and dest file structure
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				// recursive copy
				copyFolder(srcFile, destFile);
			}

		} else {
			// if file, then copy it
			// Use bytes stream to support all file types
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}

			in.close();
			out.close();
		}
	}
}