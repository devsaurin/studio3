package com.aptana.terminal.server;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import com.aptana.terminal.Activator;
import com.aptana.util.ResourceUtils;

/**
 * BuiltinCygwinConfiguration
 */
public class DefaultWindowsConfiguration implements ProcessConfiguration
{
	private static final String CYGWIN_ROOT = "/cygwin/";
	private static final String REDTTY_EXE = "/cygwin/bin/redtty.exe";
	private static final boolean useCmdExe = false;

	/**
	 * BuiltinCygwinConfiguration
	 */
	public DefaultWindowsConfiguration()
	{
	}
	
	/*
	 * @see com.aptana.terminal.server.ProcessConfiguration#afterStart(com.aptana.terminal.server.ProcessWrapper)
	 */
	@Override
	public void afterStart(ProcessWrapper wrapper)
	{
		if (useCmdExe)
		{
			// Turn on filtering
			String marker = UUID.randomUUID().toString();
			Pattern filter = Pattern.compile("^" + marker + "[\\r\\n]+", Pattern.MULTILINE);
			
			wrapper.setStandardOutputFilter(filter);
			
			// Set current directory, if needed
			String startingDirectory = wrapper.getStartingDirectory();
			
			if (startingDirectory != null && startingDirectory.length() > 0)
			{
				File dir = new File(startingDirectory);
				
				if (dir.exists())
				{
					wrapper.sendText("cd \"`cygpath \"" + dir.getAbsolutePath() + "\"`\"\n");
				}
			}
			
			// startup cmd.exe and turn filtering off
			String command = "cmd.exe /K '@echo " + marker + "'";
			
			wrapper.sendText(command + ";exit\n");
		}
	}

	/*
	 * @see com.aptana.terminal.server.ProcessConfiguration#beforeStart(com.aptana.terminal.server.ProcessWrapper, java.lang.ProcessBuilder)
	 */
	@Override
	public void beforeStart(ProcessWrapper wrapper, ProcessBuilder builder)
	{
	}

	/*
	 * @see com.aptana.terminal.server.ProcessConfiguration#getCommandLineArguments()
	 */
	@Override
	public List<String> getCommandLineArguments()
	{
		return new ArrayList<String>();
	}

	/*
	 * @see com.aptana.terminal.server.ProcessConfiguration#getPlatform()
	 */
	@Override
	public String getPlatform()
	{
		return Platform.OS_WIN32;
	}

	/*
	 * @see com.aptana.terminal.server.ProcessConfiguration#getProcessName()
	 */
	@Override
	public String getProcessName()
	{
		URL url = FileLocator.find(Activator.getDefault().getBundle(), new Path(REDTTY_EXE), null); //$NON-NLS-1$
		
		return ResourceUtils.resourcePathToString(url);
	}

	/**
	 * @see com.aptana.terminal.server.ProcessConfiguration#isValid()
	 */
	@Override
	public boolean isValid()
	{
		return true;
	}

	/*
	 * @see com.aptana.terminal.server.ProcessConfiguration#setupEnvironment(java.util.Map)
	 */
	@Override
	public void setupEnvironment(Map<String, String> env)
	{
		URL url = FileLocator.find(Activator.getDefault().getBundle(), new Path(CYGWIN_ROOT), null); //$NON-NLS-1$
		String root = ResourceUtils.resourcePathToString(url);
		
		if (root != null)
		{
			String usrLocalBin = root + "\\usr\\local\\bin";
			String usrBin = root + "\\usr\\bin";
			String bin = root + "\\bin";
			String path = usrLocalBin + File.pathSeparator + usrBin + File.pathSeparator + bin;
			
			if (env.containsKey("Path"))
			{
				path += File.pathSeparator + env.get("Path");
			}
			
			env.put("Path", path);
		}
		
		env.put("TERM", "xterm-color"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
