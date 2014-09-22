package org.evosuite.symbolic.expr;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.evosuite.symbolic.expr.bv.IntegerBinaryExpression;
import org.evosuite.symbolic.expr.bv.IntegerComparison;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.IntegerUnaryExpression;
import org.evosuite.symbolic.expr.bv.IntegerVariable;
import org.evosuite.symbolic.expr.bv.RealComparison;
import org.evosuite.symbolic.expr.bv.RealToIntegerCast;
import org.evosuite.symbolic.expr.bv.RealUnaryToIntegerExpression;
import org.evosuite.symbolic.expr.bv.StringBinaryComparison;
import org.evosuite.symbolic.expr.bv.StringBinaryToIntegerExpression;
import org.evosuite.symbolic.expr.bv.StringMultipleComparison;
import org.evosuite.symbolic.expr.bv.StringMultipleToIntegerExpression;
import org.evosuite.symbolic.expr.bv.StringToIntegerCast;
import org.evosuite.symbolic.expr.bv.StringUnaryToIntegerExpression;
import org.evosuite.symbolic.expr.fp.IntegerToRealCast;
import org.evosuite.symbolic.expr.fp.RealBinaryExpression;
import org.evosuite.symbolic.expr.fp.RealConstant;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealVariable;
import org.evosuite.symbolic.expr.reader.StringReaderExpr;
import org.evosuite.symbolic.expr.str.IntegerToStringCast;
import org.evosuite.symbolic.expr.str.RealToStringCast;
import org.evosuite.symbolic.expr.str.StringBinaryExpression;
import org.evosuite.symbolic.expr.str.StringConstant;
import org.evosuite.symbolic.expr.str.StringMultipleExpression;
import org.evosuite.symbolic.expr.str.StringUnaryExpression;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.symbolic.expr.token.HasMoreTokensExpr;
import org.evosuite.symbolic.expr.token.NewTokenizerExpr;
import org.evosuite.symbolic.expr.token.NextTokenizerExpr;
import org.evosuite.symbolic.expr.token.StringNextTokenExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpressionExecutor implements ExpressionVisitor<Object, Void> {

	protected static final Logger log = LoggerFactory
			.getLogger(ExpressionExecutor.class);

	@Override
	public Object visit(IntegerBinaryExpression n, Void arg) {
		Long leftLong = (Long) n.getLeftOperand().accept(this, null);
		Long rightLong = (Long) n.getRightOperand().accept(this, null);

		long leftVal = leftLong.longValue();
		long rightVal = rightLong.longValue();

		Operator op = n.getOperator();
		switch (op) {

		case SHL:
			return leftVal << rightVal;
		case SHR:
			return leftVal >> rightVal;
		case USHR:
			return leftVal >>> rightVal;
		case AND:
		case IAND:
			return leftVal & rightVal;
		case OR:
		case IOR:
			return leftVal | rightVal;
		case XOR:
		case IXOR:
			return leftVal ^ rightVal;
		case DIV:
			return leftVal / rightVal;
		case MUL:
			return leftVal * rightVal;
		case MINUS:
			return leftVal - rightVal;
		case PLUS:
			return leftVal + rightVal;
		case REM:
			return leftVal % rightVal;
		case MAX:
			return Math.max(leftVal, rightVal);
		case MIN:
			return Math.min(leftVal, rightVal);

		default:
			log.warn("IntegerBinaryExpression: unimplemented operator: " + op);
			return null;
		}

	}

	@Override
	public Object visit(IntegerComparison n, Void arg) {
		log.warn("IntegerComparison.execute() invokation");
		throw new IllegalStateException("This method should not be invoked");
	}

	@Override
	public Object visit(IntegerConstant n, Void arg) {
		return n.getConcreteValue();
	}

	@Override
	public Object visit(IntegerUnaryExpression n, Void arg) {

		Long longObject = (Long) n.getOperand().accept(this, null);

		long leftVal = longObject.longValue();

		Operator op = n.getOperator();
		switch (op) {

		case NEG:
			return -leftVal;
		case ABS:
			return Math.abs(leftVal);
		case GETNUMERICVALUE:
			return (long) Character.getNumericValue((char) leftVal);
		case ISLETTER:
			return Character.isLetter((char) leftVal) ? 1L : 0L;
		case ISDIGIT:
			return Character.isDigit((char) leftVal) ? 1L : 0L;

		default:
			log.warn("IntegerUnaryExpression: unimplemented operator: " + op);
			return null;
		}
	}

	@Override
	public Object visit(IntegerVariable n, Void arg) {
		return n.getConcreteValue();
	}

	@Override
	public Object visit(RealComparison n, Void arg) {
		log.warn("RealComparison.execute() invokation");
		throw new IllegalStateException("This method should not be invoked");
	}

	@Override
	public Object visit(RealToIntegerCast n, Void arg) {
		Double doubleObject = (Double) n.getArgument().accept(this, null);
		return doubleObject.longValue();
	}

	@Override
	public Object visit(RealUnaryToIntegerExpression n, Void arg) {
		Double doubleObject = (Double) n.getOperand().accept(this, null);
		double leftVal = doubleObject.doubleValue();

		Operator op = n.getOperator();
		switch (op) {

		case ROUND:
			return Math.round(leftVal);
		case GETEXPONENT:
			return (long) Math.getExponent(leftVal);

		default:
			log.warn("IntegerUnaryExpression: unimplemented operator: " + op);
			return null;
		}
	}

	@Override
	public Object visit(StringBinaryComparison n, Void arg) {
		String first = (String) n.getLeftOperand().accept(this, null);
		String second = (String) n.getRightOperand().accept(this, null);

		Operator op = n.getOperator();
		switch (op) {
		case EQUALSIGNORECASE:
			return first.equalsIgnoreCase(second) ? 1L : 0L;
		case EQUALS:
			return first.equals(second) ? 1L : 0L;
		case ENDSWITH:
			return first.endsWith(second) ? 1L : 0L;
		case CONTAINS:
			return first.contains(second) ? 1L : 0L;
		case PATTERNMATCHES:
			return second.matches(first) ? 1L : 0L;
		case APACHE_ORO_PATTERN_MATCHES: {
			Perl5Matcher matcher = new Perl5Matcher();
			Perl5Compiler compiler = new Perl5Compiler();
			Pattern pattern;
			try {
				pattern = compiler.compile(first);
			} catch (MalformedPatternException e) {
				throw new RuntimeException(e);
			}
			return matcher.matches(second, pattern) ? 1L : 0L;

		}
		default:
			log.warn("StringComparison: unimplemented operator!" + op);
			return null;
		}

	}

	@Override
	public Object visit(StringBinaryToIntegerExpression n, Void arg) {
		String first = (String) n.getLeftOperand().accept(this, null);
		Object second = (Object) n.getRightOperand().accept(this, null);

		Operator op = n.getOperator();
		switch (op) {

		// returns Int
		case COMPARETO: {
			String string = (String) second;
			return (long) first.compareTo(string);
		}
		case COMPARETOIGNORECASE: {
			String string = (String) second;
			return (long) first.compareToIgnoreCase(string);
		}
		case INDEXOFC: {
			long ch = (Long) second;
			return (long) first.indexOf((char) ch);
		}
		case INDEXOFS: {
			String string = (String) second;
			return (long) first.indexOf(string);
		}
		case LASTINDEXOFC: {
			long ch = (Long) second;
			return (long) first.lastIndexOf((char) ch);
		}
		case LASTINDEXOFS: {
			String string = (String) second;
			return (long) first.lastIndexOf(string);
		}
		case CHARAT: {
			int indx = ((Long) second).intValue();
			return (long) first.charAt(indx);
		}
		default:
			log.warn("StringBinaryToIntegerExpression: unimplemented operator! Operator"
					+ op.toString());
			return null;
		}

	}

	@Override
	public Object visit(StringMultipleComparison n, Void arg) {
		String first = (String) n.getLeftOperand().accept(this, null);
		String second = (String) n.getRightOperand().accept(this, null);
		ArrayList<Expression<?>> other_v = n.getOther();

		Operator op = n.getOperator();
		switch (op) {
		case STARTSWITH:
			long start = (Long) other_v.get(0).accept(this, null);

			return first.startsWith(second, (int) start) ? 1L : 0L;

		case REGIONMATCHES:
			long frstStart = (Long) other_v.get(0).accept(this, null);
			long secStart = (Long) other_v.get(1).accept(this, null);
			long length = (Long) other_v.get(2).accept(this, null);
			long ignoreCase = (Long) other_v.get(3).accept(this, null);

			return first.regionMatches(ignoreCase != 0, (int) frstStart,
					second, (int) secStart, (int) length) ? 1L : 0L;
		default:
			log.warn("StringMultipleComparison: unimplemented operator!");
			return null;
		}
	}

	@Override
	public Object visit(StringMultipleToIntegerExpression n, Void arg) {
		String first = (String) n.getLeftOperand().accept(this, null);
		Object second = (Object) n.getRightOperand().accept(this, null);
		ArrayList<Expression<?>> other_v = n.getOther();

		long secLong, thrdLong;
		String secStr;

		Operator op = n.getOperator();
		switch (op) {

		// returns int
		case INDEXOFCI:
			secLong = (Long) second;
			thrdLong = (Long) other_v.get(0).accept(this, null);
			return (long) first.indexOf((int) secLong, (int) thrdLong);
		case INDEXOFSI:
			secStr = (String) second;
			thrdLong = (Long) other_v.get(0).accept(this, null);
			return (long) first.indexOf(secStr, (int) thrdLong);
		case LASTINDEXOFCI:
			secLong = (Long) second;
			thrdLong = (Long) other_v.get(0).accept(this, null);
			return (long) first.lastIndexOf((int) secLong, (int) thrdLong);
		case LASTINDEXOFSI:
			secStr = (String) second;
			thrdLong = (Long) other_v.get(0).accept(this, null);
			return (long) first.lastIndexOf(secStr, (int) thrdLong);

		default:
			log.warn("StringMultipleToIntegerExpression: unimplemented operator: "
					+ op);
			return null;
		}
	}

	@Override
	public Object visit(StringToIntegerCast n, Void arg) {
		String str = (String) n.getArgument().accept(this, null);
		return Long.parseLong(str);

	}

	@Override
	public Object visit(StringUnaryToIntegerExpression n, Void arg) {
		String exOn = (String) n.getOperand().accept(this, null);

		Operator op = n.getOperator();
		switch (op) {

		case LENGTH:
			return (long) exOn.length();

		default:
			log.warn("StringUnaryExpression: unimplemented operator!");
			return null;
		}
	}

	@Override
	public Object visit(IntegerToRealCast n, Void arg) {
		Long exprVal = (Long) n.getArgument().accept(this, null);
		return exprVal.doubleValue();
	}

	@Override
	public Object visit(RealBinaryExpression n, Void arg) {

		Double leftDouble = (Double) n.getLeftOperand().accept(this, null);
		Double rightDouble = (Double) n.getRightOperand().accept(this, null);

		double leftVal = leftDouble.doubleValue();
		double rightVal = rightDouble.doubleValue();

		Operator op = n.getOperator();
		switch (op) {

		case DIV:
			return leftVal / rightVal;
		case MUL:
			return leftVal * rightVal;
		case MINUS:
			return leftVal - rightVal;
		case PLUS:
			return leftVal + rightVal;
		case REM:
			return leftVal % rightVal;
		case ATAN2:
			return Math.atan2(leftVal, rightVal);
		case COPYSIGN:
			return Math.copySign(leftVal, rightVal);
		case HYPOT:
			return Math.hypot(leftVal, rightVal);
		case IEEEREMAINDER:
			return Math.IEEEremainder(leftVal, rightVal);
		case MAX:
			return Math.max(leftVal, rightVal);
		case MIN:
			return Math.min(leftVal, rightVal);
		case NEXTAFTER:
			return Math.nextAfter(leftVal, rightVal);
		case POW:
			return Math.pow(leftVal, rightVal);
		case SCALB:
			return Math.scalb(leftVal, (int) rightVal);

		default:
			log.warn("IntegerBinaryExpression: unimplemented operator: " + op);
			return null;
		}

	}

	@Override
	public Object visit(RealConstant n, Void arg) {
		return n.getConcreteValue();
	}

	@Override
	public Object visit(RealUnaryExpression n, Void arg) {
		Double doubleObject = (Double) n.getOperand().accept(this, null);
		double doubleVal = doubleObject.doubleValue();

		Operator op = n.getOperator();
		switch (op) {

		case ABS:
			return Math.abs(doubleVal);
		case ACOS:
			return Math.acos(doubleVal);
		case ASIN:
			return Math.asin(doubleVal);
		case ATAN:
			return Math.atan(doubleVal);
		case CBRT:
			return Math.cbrt(doubleVal);
		case CEIL:
			return Math.ceil(doubleVal);
		case COS:
			return Math.cos(doubleVal);
		case COSH:
			return Math.cosh(doubleVal);
		case EXP:
			return Math.exp(doubleVal);
		case EXPM1:
			return Math.expm1(doubleVal);
		case FLOOR:
			return Math.floor(doubleVal);
		case LOG:
			return Math.log(doubleVal);
		case LOG10:
			return Math.log10(doubleVal);
		case LOG1P:
			return Math.log1p(doubleVal);
		case NEG:
			return -doubleVal;
		case NEXTUP:
			return Math.nextUp(doubleVal);
		case RINT:
			return Math.rint(doubleVal);
		case SIGNUM:
			return Math.signum(doubleVal);
		case SIN:
			return Math.sin(doubleVal);
		case SINH:
			return Math.sinh(doubleVal);
		case SQRT:
			return Math.sqrt(doubleVal);
		case TAN:
			return Math.tan(doubleVal);
		case TANH:
			return Math.tanh(doubleVal);
		case TODEGREES:
			return Math.toDegrees(doubleVal);
		case TORADIANS:
			return Math.toRadians(doubleVal);
		case ULP:
			return Math.ulp(doubleVal);

		default:
			log.warn("RealUnaryExpression: unimplemented operator: " + op);
			return null;
		}

	}

	@Override
	public Object visit(RealVariable n, Void arg) {
		return n.getConcreteValue();
	}

	@Override
	public Object visit(StringReaderExpr n, Void arg) {

		String conc_string = (String) n.getString().accept(this, null);
		if (n.getReaderPosition() >= conc_string.length()) {
			return -1L;
		} else {
			return (long) conc_string.charAt(n.getReaderPosition());
		}
	}

	@Override
	public Object visit(IntegerToStringCast n, Void arg) {
		Long exprVal = (Long) n.getArgument().accept(this, null);
		return Long.toString(exprVal);
	}

	@Override
	public Object visit(RealToStringCast n, Void arg) {
		Double doubleObject = (Double) n.getArgument().accept(this, null);
		return Double.toString(doubleObject);
	}

	@Override
	public Object visit(StringBinaryExpression n, Void arg) {
		String first = (String) n.getLeftOperand().accept(this, null);
		Object second = (Object) n.getRightOperand().accept(this, null);

		Operator op = n.getOperator();
		switch (op) {

		// returns String
		case CONCAT: {
			String string = (String) second;
			return first.concat(string);
		}
		case APPEND_BOOLEAN: {
			Long sndLong = (Long) second;
			boolean booleabValue = sndLong == 0 ? false : true;
			return first + booleabValue;
		}
		case APPEND_CHAR: {
			Long sndLong = (Long) second;
			char charValue = (char) sndLong.longValue();
			return first + charValue;
		}
		case APPEND_INTEGER: {
			Long sndLong = (Long) second;
			return first + sndLong;
		}
		case APPEND_REAL: {
			Double sndLong = (Double) second;
			return first + sndLong;
		}
		case APPEND_STRING: {
			String string = (String) second;
			return first + (string);
		}

		default:
			log.warn("StringBinaryExpression: unimplemented operator! Operator"
					+ op.toString());
			return null;
		}

	}

	@Override
	public Object visit(StringConstant n, Void arg) {
		return n.getConcreteValue();
	}

	@Override
	public Object visit(StringMultipleExpression n, Void arg) {
		String first = (String) n.getLeftOperand().accept(this, null);
		Object right = (Object) n.getRightOperand().accept(this, null);
		ArrayList<Expression<?>> other_v = n.getOther();
		long secLong, thrdLong;
		String secStr, thrdStr;

		Operator op = n.getOperator();
		switch (op) {

		// returns string
		case SUBSTRING: {
			secLong = (Long) right;
			thrdLong = (Long) other_v.get(0).accept(this, null);
			return first.substring((int) secLong, (int) thrdLong);
		}
		case REPLACEC:
			secLong = (Long) right;
			thrdLong = (Long) other_v.get(0).accept(this, null);
			return first.replace((char) secLong, (char) thrdLong);
		case REPLACECS:
			secStr = (String) right;
			thrdStr = (String) other_v.get(0).accept(this, null);
			return first.replace(secStr, thrdStr);
		case REPLACEALL:
			secStr = (String) right;
			thrdStr = (String) other_v.get(0).accept(this, null);
			return first.replaceAll(secStr, thrdStr);
		case REPLACEFIRST:
			secStr = (String) right;
			thrdStr = (String) other_v.get(0).accept(this, null);
			return first.replaceFirst(secStr, thrdStr);
		default:
			log.warn("StringMultipleExpression: unimplemented operator: " + op);
			return null;
		}

	}

	@Override
	public Object visit(StringUnaryExpression n, Void arg) {
		String exOn = (String) n.getOperand().accept(this, null);

		Operator op = n.getOperator();
		switch (op) {

		case TOLOWERCASE:
			return exOn.toLowerCase();
		case TOUPPERCASE:
			return exOn.toUpperCase();
		case TRIM:
			return exOn.trim();

		default:
			log.warn("StringUnaryExpression: unimplemented operator!" + op);
			return null;
		}
	}

	@Override
	public Object visit(StringVariable n, Void arg) {
		return n.getConcreteValue();
	}

	@Override
	public Object visit(HasMoreTokensExpr n, Void arg) {
		StringTokenizer tokenizer = (StringTokenizer) n.getTokenizerExpr()
				.accept(this, null);
		return tokenizer.hasMoreTokens() ? 1L : 0L;
	}

	@Override
	public Object visit(NewTokenizerExpr n, Void arg) {
		String stringVal = (String) n.getString().accept(this, null);
		String delimVal = (String) n.getDelimiter().accept(this, null);
		StringTokenizer tokenizer = new StringTokenizer(stringVal, delimVal);
		return tokenizer;
	}

	@Override
	public Object visit(NextTokenizerExpr n, Void arg) {
		StringTokenizer tokenizer = (StringTokenizer) n.getTokenizerExpr()
				.accept(this, null);
		tokenizer.nextToken();
		return tokenizer;
	}

	@Override
	public Object visit(StringNextTokenExpr n, Void arg) {
		StringTokenizer tokenizer = (StringTokenizer) n.getTokenizerExpr().accept(this,null);
		return tokenizer.nextToken();
	}

}