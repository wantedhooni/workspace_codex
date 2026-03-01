package com.derivops.mvp.common.rsql;

import com.derivops.mvp.common.BadRequestException;
import java.util.ArrayList;
import java.util.List;

public final class RsqlParser {

    private RsqlParser() {
    }

    public static List<List<RsqlExpression>> parseToOrAndGroups(String filter) {
        if (filter == null || filter.isBlank()) {
            return List.of();
        }

        List<String> orParts = splitTopLevel(filter, ',');
        List<List<RsqlExpression>> groups = new ArrayList<>();
        for (String orPart : orParts) {
            if (orPart.isBlank()) {
                continue;
            }
            List<String> andParts = splitTopLevel(orPart, ';');
            List<RsqlExpression> andGroup = new ArrayList<>();
            for (String token : andParts) {
                if (token.isBlank()) {
                    continue;
                }
                andGroup.add(parseExpression(token.trim()));
            }
            if (!andGroup.isEmpty()) {
                groups.add(andGroup);
            }
        }
        return groups;
    }

    private static RsqlExpression parseExpression(String raw) {
        RsqlOperator operator = RsqlOperator.fromExpression(raw);
        if (operator == null) {
            throw new BadRequestException("Invalid RSQL expression: " + raw);
        }

        int idx = raw.indexOf(operator.token());
        String selector = raw.substring(0, idx).trim();
        String argumentPart = raw.substring(idx + operator.token().length()).trim();

        if (selector.isBlank()) {
            throw new BadRequestException("RSQL selector is empty: " + raw);
        }

        return new RsqlExpression(selector, operator, parseArgument(raw, operator, argumentPart));
    }

    private static RsqlArgument parseArgument(String raw, RsqlOperator operator, String argumentPart) {
        if (!operator.isMultiValue()) {
            return new RsqlArgument.SingleValue(stripQuotes(argumentPart));
        }
        if (!(argumentPart.startsWith("(") && argumentPart.endsWith(")"))) {
            throw new BadRequestException("RSQL IN/OUT requires parentheses: " + raw);
        }

        List<String> arguments = splitTopLevel(argumentPart.substring(1, argumentPart.length() - 1), ',').stream()
                .map(RsqlParser::stripQuotes)
                .filter(argument -> !argument.isBlank())
                .toList();

        if (arguments.isEmpty()) {
            throw new BadRequestException("RSQL IN/OUT requires at least one value: " + raw);
        }
        return new RsqlArgument.MultiValue(arguments);
    }

    private static String stripQuotes(String value) {
        String trimmed = value.trim();
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
                || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static List<String> splitTopLevel(String source, char separator) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        char quote = 0;

        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);

            if (quote != 0) {
                current.append(c);
                if (c == quote && (i == 0 || source.charAt(i - 1) != '\\')) {
                    quote = 0;
                }
                continue;
            }

            if (c == '\'' || c == '"') {
                quote = c;
                current.append(c);
                continue;
            }

            if (c == '(') {
                depth++;
                current.append(c);
                continue;
            }

            if (c == ')') {
                depth--;
                current.append(c);
                continue;
            }

            if (c == separator && depth == 0) {
                result.add(current.toString().trim());
                current.setLength(0);
                continue;
            }

            current.append(c);
        }

        result.add(current.toString().trim());
        return result;
    }
}
