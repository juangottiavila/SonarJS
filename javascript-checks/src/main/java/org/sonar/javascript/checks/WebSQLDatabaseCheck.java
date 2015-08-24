/*
 * SonarQube JavaScript Plugin
 * Copyright (C) 2011 SonarSource and Eriks Nukis
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.javascript.checks;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.javascript.tree.symbols.type.ObjectType.WebApiType;
import org.sonar.plugins.javascript.api.tree.Tree.Kind;
import org.sonar.plugins.javascript.api.tree.expression.CallExpressionTree;
import org.sonar.plugins.javascript.api.tree.expression.ExpressionTree;
import org.sonar.plugins.javascript.api.tree.expression.IdentifierTree;
import org.sonar.plugins.javascript.api.tree.expression.MemberExpressionTree;
import org.sonar.plugins.javascript.api.visitors.BaseTreeVisitor;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

@Rule(
  key = "S2817",
  name = "Web SQL databases should not be used",
  priority = Priority.CRITICAL,
  tags = {Tags.HTML5, Tags.SECURITY, Tags.OWASP_A6, Tags.OWASP_A9})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.SECURITY_FEATURES)
@SqaleConstantRemediation("2h")
public class WebSQLDatabaseCheck extends BaseTreeVisitor {

  private static final String MESSAGE = "Convert this use of a Web SQL database to another technology";
  private static final String OPEN_DATABASE = "openDatabase";

  @Override
  public void visitCallExpression(CallExpressionTree tree) {
    ExpressionTree callee = tree.callee();

    if (isOpenDatabase(callee)) {
      getContext().addIssue(this, tree, MESSAGE);

    } else if (callee.is(Kind.DOT_MEMBER_EXPRESSION)) {
      MemberExpressionTree memberExpr = (MemberExpressionTree) callee;
      boolean isWindowObject = memberExpr.object().types().contains(WebApiType.WINDOW) || memberExpr.object().is(Kind.THIS);

      if (isWindowObject && isOpenDatabase(memberExpr.property())) {
        getContext().addIssue(this, tree, MESSAGE);
      }
    }

    super.visitCallExpression(tree);
  }

  private static boolean isOpenDatabase(ExpressionTree callee) {
    return callee instanceof IdentifierTree && ((IdentifierTree) callee).name().equals(OPEN_DATABASE);
  }
}
