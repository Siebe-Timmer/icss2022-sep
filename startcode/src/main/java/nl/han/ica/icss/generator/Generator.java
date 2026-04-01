package nl.han.ica.icss.generator;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.selectors.*;

import java.util.List;

public class Generator {

	public String generate(AST ast) {
		return generateStylesheet(ast.root);
	}

	private String generateStylesheet(Stylesheet stylesheet) {
		StringBuilder stringBuilder = new StringBuilder();
		for (ASTNode child : stylesheet.getChildren()) {
			if (child instanceof Stylerule) {
				stringBuilder.append(generateStylerule((Stylerule) child)).append("\n");
			}
		}
		return stringBuilder.toString();
	}

	private String generateStylerule(Stylerule stylerule) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(generateSelector(stylerule.selectors));
		for (ASTNode child : stylerule.body) {
			if (child instanceof Declaration) {
				stringBuilder.append("  ").append(generateDeclaration((Declaration) child)).append("\n");
			}
		}
		stringBuilder.append("}");
		return stringBuilder.toString();
	}

	private String generateSelector(List<Selector> selectors) {
		StringBuilder stringBuilder = new StringBuilder();
		for (Selector selector : selectors) {
			if (selector instanceof TagSelector) {
				stringBuilder.append(((TagSelector) selector).tag);
			} else if (selector instanceof ClassSelector) {
				stringBuilder.append(((ClassSelector) selector).cls);
			} else if (selector instanceof IdSelector) {
				stringBuilder.append(((IdSelector) selector).id);
			}
		}
		return stringBuilder.append(" {\n").toString();
	}

	private String generateDeclaration(Declaration declaration) {
		return declaration.property.name + ": " + generateLiteral((Literal) declaration.expression) + ";";
	}

	private String generateLiteral(Literal literal) {
		if (literal instanceof ColorLiteral)      return ((ColorLiteral) literal).value;
		if (literal instanceof PixelLiteral)      return ((PixelLiteral) literal).value + "px";
		if (literal instanceof PercentageLiteral) return ((PercentageLiteral) literal).value + "%";
		if (literal instanceof ScalarLiteral)     return String.valueOf(((ScalarLiteral) literal).value);
		return "";
	}
}