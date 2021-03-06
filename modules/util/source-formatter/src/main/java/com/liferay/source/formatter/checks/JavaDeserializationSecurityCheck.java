/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.source.formatter.checks;

import com.liferay.portal.kernel.util.StringBundler;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Hugo Huijser
 */
public class JavaDeserializationSecurityCheck extends BaseFileCheck {

	public JavaDeserializationSecurityCheck(
		List<String> secureDeserializationExcludes,
		List<String> runOutsidePortalExcludes) {

		_secureDeserializationExcludes = secureDeserializationExcludes;
		_runOutsidePortalExcludes = runOutsidePortalExcludes;
	}

	@Override
	protected String doProcess(
		String fileName, String absolutePath, String content) {

		if (fileName.contains("/test/") ||
			fileName.contains("/testIntegration/") ||
			isExcludedPath(_secureDeserializationExcludes, absolutePath)) {

			return content;
		}

		_checkDeserializationSecurity(fileName, content, absolutePath);

		return content;
	}

	private void _checkDeserializationSecurity(
		String fileName, String content, String absolutePath) {

		for (Pattern vulnerabilityPattern :
				_javaSerializationVulnerabilityPatterns) {

			Matcher matcher = vulnerabilityPattern.matcher(content);

			if (!matcher.matches()) {
				continue;
			}

			StringBundler sb = new StringBundler(3);

			if (isExcludedPath(_runOutsidePortalExcludes, absolutePath)) {
				sb.append("Possible Java Serialization Remote Code Execution ");
				sb.append("vulnerability using ");
			}
			else {
				sb.append("Use ProtectedObjectInputStream instead of ");
			}

			sb.append(matcher.group(1));

			addMessage(fileName, sb.toString());
		}
	}

	private final Pattern[] _javaSerializationVulnerabilityPatterns =
		new Pattern[] {
			Pattern.compile(
				".*(new [a-z\\.\\s]*ObjectInputStream).*", Pattern.DOTALL),
			Pattern.compile(
				".*(extends [a-z\\.\\s]*ObjectInputStream).*", Pattern.DOTALL)
		};
	private final List<String> _runOutsidePortalExcludes;
	private final List<String> _secureDeserializationExcludes;

}