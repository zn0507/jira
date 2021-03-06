package com;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.domain.Issue;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class ShowIssue implements ToolWindowFactory {
	private JTextField issueNumber;
	private JComboBox projectNumber;
	private JPanel selectIssue;
	private JScrollPane issueContent;
	private JPanel showIssue;
	private JButton ok;
	private JTextPane issue;
	private JButton showPicture;
	private static JiraRestClient client;
	private Map<String, String> pictures = new HashMap<>();

	public static void reLogin(JiraSettingState setting) {
		client = JiraUtils.getJiraClient(setting.getHost(), setting.getUser(), setting.getPassword());
	}

	@Override
	public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
		Content content = contentFactory.createContent(showIssue, "", false);
		JiraSettingState setting = JiraSetting.getInstance().getState();
		if (setting == null) {
			issue.setText("<html><h1>请登录</h1></html>");
		} else {
			reLogin(setting);
		}
		initProjectNumber();
		initListener();
//		addStyle();
		toolWindow.getContentManager().addContent(content);
	}

	private void initProjectNumber() {

		if (client == null) {
			projectNumber.addItem("GLD");
			projectNumber.addItem("TAM");
			projectNumber.addItem("FE");
			projectNumber.addItem("MIM");
			projectNumber.addItem("AER");
			projectNumber.addItem("TNT");
		} else {
			for (String pro : JiraUtils.getAllProjectsKey(client)) {
				projectNumber.addItem(pro);
			}
		}

	}

	private void initListener() {
		ActionListener loginListener = e -> {
			addStyle();
			issue.setText("");
			String issueNumberText = issueNumber.getText();
			String userText = projectNumber.getSelectedItem().toString();
			Issue issueAll = JiraUtils.getIssueByKey(client, userText + "-" + issueNumberText);
			if (client == null) {
				issueAddString("没有登录", true);
			} else if (issueAll == null) {
				issueAddString("没有此Issue", true);
			} else {
				issueAddString("Title:\n", true);
				issueAddString(issueAll.getSummary(), false);
				issueAddString("\nDescription:\n", true);
				issueAddString(issueAll.getDescription(), false);
				issueAddString("\nComments:\n", true);
				issueAddString(JiraUtils.issueCommentsString(issueAll), false);
				pictures = JiraUtils.getIssuePhotos(issueAll);
			}
		};

		ActionListener pictureListener = e -> {
			ShowPicture showPicture = new ShowPicture(pictures, client);
			showPicture.showPictureFrame();
		};

		showPicture.addActionListener(pictureListener);
		ok.addActionListener(loginListener);
	}

	private void addStyle() {
		JiraSettingState setting = JiraSetting.getInstance().getState();
		Style def = issue.getStyledDocument().addStyle(null, null);
		StyleConstants.setFontFamily(def,"family");
		StyleConstants.setFontSize(def, setting.getContentSize());
//		StyleConstants.setForeground(def, Color.lightGray);
		StyleConstants.setForeground(def, Color.decode(setting.getContentColor()));
		Style normal = issue.addStyle("normal", def);
		Style s = issue.addStyle("bold", normal);
//		StyleConstants.setForeground(s, Color.ORANGE);
		StyleConstants.setForeground(s, Color.decode(setting.getTitleColor()));
		StyleConstants.setBold(s, true);
		StyleConstants.setFontSize(s, setting.getTitleSize());
		issue.setParagraphAttributes(normal, true);

	}

	private void issueAddString(String content, boolean bold) {
		String b = bold ? "bold" : "normal";
		try {
			issue.getDocument().insertString(issue.getDocument().getLength(), content, issue.getStyle(b));
		} catch (Exception e) {
			System.out.println("issuecontentError");
		}
	}
}
