package com.ntscorp.auto_client.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.ntscorp.auto_client.Utilities;

class SendResponsTimeReport implements Utilities {

	Session session;
	Channel channel;
	ChannelSftp sftpChannel;
	Properties config;

	/**
	 * Api Response Time 데이터를 전송하기 위한 Server Connection
	 * 
	 * @return void
	 * @throws JSchException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	void connect() throws JSchException, IOException, InterruptedException {
		config = new Properties();
		config.put("StrictHostKeyChecking", "no");

		JSch jsch = new JSch();
		session = jsch.getSession("root", "10.12.54.139");
		session.setConfig(config);
		session.setPassword("prism123");
		session.connect();

		channel = session.openChannel("sftp");
		channel.connect();
		sftpChannel = (ChannelSftp) channel;
	}

	/**
	 * Api Response Time 데이터를 전송한 이후 Server Disconnection
	 * 
	 * @return void
	 */
	void disconnect() {
		if (session.isConnected()) {
			System.out.println("disconnecting...");
			sftpChannel.disconnect();
			channel.disconnect();
			session.disconnect();
		}
	}

	/**
	 * Api Response Time 데이터 upload
	 * 
	 * @param filePath Api Response Time Data가 저장되어 있는 경로
	 * @return void
	 */
	void upload(String filePath) throws Exception {
		FileInputStream fis = null;
		String reportPath = "/var/www/html/api-result";
		String projectName = getDate();

		// 접속
		connect();
		// 프로젝트 네임에 공백 있는경우 언더바로 치환
		projectName.replaceAll(" ", "_");
		
		try {
			// 프로젝트 폴더 있는지 확인
			boolean isfileexistsinlocation = isPresentProject(reportPath, projectName);
	
			if (!isfileexistsinlocation) {
				sftpChannel.mkdir(reportPath + "/" + projectName);
			}

			// Change to directory
			sftpChannel.cd(reportPath + "/" + projectName);

			// Upload file
			File file = new File(filePath);
			// 입력 파일을 가져온다.
			fis = new FileInputStream(file);
			// 파일을 업로드한다.
			sftpChannel.put(fis, file.getName());

			fis.close();
			System.out.println("File uploaded successfully - " + file.getAbsolutePath());

		} catch (Exception e) {
			e.printStackTrace();
		}

		// 연결 종료
		disconnect();
	}
	
	/**
	 * Project Directory가 이미 존재 하는 지 확인
	 * 
	 * @param reportPath report가 저장되어 있는 경로
	 * @param projectName 프로젝트 명
	 * @return bool 
	 * @throws SftpException
	 */
	private Boolean isPresentProject(String reportPath, String projectName) {
		try {
			String fileList = sftpChannel.ls(reportPath).toString();
			return fileList.contains(projectName);
		} catch (SftpException e) {
			return false;
		}
	}
}
