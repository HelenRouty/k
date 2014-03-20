package org.kframework.krun.tasks;

import org.kframework.krun.K;
import org.kframework.utils.maude.MaudeRun;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class MaudeTask extends Thread {
    // private static final String LOG_FILE = "maude.log";
    // private static final String LOGGER = "maude";
    private String _command;
    private String _outputFile;
    private String _errorFile;
    private Process _maudeProcess;
    public int returnValue;
    
    public static String maudeExe = MaudeRun.initializeMaudeExecutable();
    
    public MaudeTask(String command, String outputFile, String errorFile) {
        _command = command;
        _outputFile = outputFile;
        _errorFile = errorFile;
    }

    @Override
    public void run() {
        try {
            runMaude();
            runCommand();
            writeOutput();
            writeError();
            returnValue = _maudeProcess.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runMaude() throws IOException {
        ProcessBuilder maude = new ProcessBuilder();
        /*Map<String, String> environment = maude.environment();
        environment.put("MAUDE_LIB", System.getenv("MAUDE_LIB")); */
        List<String> commands = new ArrayList<String>();
        /*if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            commands.add(System.getenv("MAUDE_LIB") + K.fileSeparator + "maude");
        } else {*/
            //commands.add("maude");
            commands.add(maudeExe);
        //}
        commands.add("-no-wrap");
        commands.add("-no-banner");
        commands.add("-xml-log=" + K.maude_output);
        maude.command(commands); 

        Process maudeProcess = maude.start();
        _maudeProcess = maudeProcess;
    }

    private void runCommand() throws IOException {
        BufferedWriter maudeInput = new BufferedWriter(new OutputStreamWriter(_maudeProcess.getOutputStream()));
        maudeInput.write(_command + K.lineSeparator);
        maudeInput.close();
    }

    private void writeOutput() throws IOException {
        // redirect out in log file
        BufferedReader maudeOutput = new BufferedReader(new InputStreamReader(_maudeProcess.getInputStream()));
        BufferedWriter outputFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(_outputFile)));

        String line;
        while ((line = maudeOutput.readLine()) != null) {
            outputFile.write(line + K.lineSeparator);
        }
        maudeOutput.close();
        outputFile.close();
    }

    private void writeError() throws IOException {
        try (
            BufferedReader maudeError
                = new BufferedReader(new InputStreamReader(_maudeProcess.getErrorStream()));
            BufferedWriter errorFile
                = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(_errorFile)))) {

            String line;
            while ((line = maudeError.readLine()) != null) {
                errorFile.write(line + K.lineSeparator);
            }
        }
    }
}
