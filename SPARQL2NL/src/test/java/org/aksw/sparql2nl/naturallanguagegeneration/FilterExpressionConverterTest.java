/*
 * #%L
 * SPARQL2NL
 * %%
 * Copyright (C) 2015 Agile Knowledge Engineering and Semantic Web (AKSW)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
/**
 * 
 */
package org.aksw.sparql2nl.naturallanguagegeneration;

import java.util.Calendar;

import org.aksw.triple2nl.converter.DefaultIRIConverter;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.junit.Assert;
import org.junit.Test;

import simplenlg.framework.NLGElement;
import simplenlg.lexicon.Lexicon;
import simplenlg.realiser.english.Realiser;

import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_GreaterThan;
import org.apache.jena.sparql.expr.E_GreaterThanOrEqual;
import org.apache.jena.sparql.expr.E_LessThan;
import org.apache.jena.sparql.expr.E_LessThanOrEqual;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * @author Lorenz Buehmann
 * 
 */
public class FilterExpressionConverterTest {

	private DefaultIRIConverter uriConverter = new DefaultIRIConverter(SparqlEndpoint.getEndpointDBpedia());
	private FilterExpressionConverter conv = new FilterExpressionConverter(uriConverter);
	private Realiser realiser = new Realiser(Lexicon.getDefaultLexicon());

	/**
	 * Test method for
	 * {@link org.aksw.sparql2nl.naturallanguagegeneration.FilterExpressionConverter#convert(com.hp.hpl.jena.sparql.expr.Expr)}
	 * .
	 */
	@Test
	public void testConvert() {
		Expr var = new ExprVar("s");

		/*
		 * integer literals
		 */

		NodeValue value = NodeValue.makeInteger(1);

		// ?s = value
		Expr expr = new E_Equals(var, value);
		NLGElement element = conv.convert(expr);
		String text = realiser.realise(element).getRealisation();
		System.out.println(expr + " --> " + text);
		Assert.assertEquals("Conversion failed.", "?s is equal to 1", text);

		// ?s > value
		expr = new E_GreaterThan(var, value);
		element = conv.convert(expr);
		text = realiser.realise(element).getRealisation();
		System.out.println(expr + " --> " + text);
		Assert.assertEquals("Conversion failed.", "?s is greater than 1", text);

		// ?s >= value
		expr = new E_GreaterThanOrEqual(var, value);
		element = conv.convert(expr);
		text = realiser.realise(element).getRealisation();
		System.out.println(expr + " --> " + text);
		Assert.assertEquals("Conversion failed.", "?s is greater than or equal to 1", text);

		// ?s < value
		expr = new E_LessThan(var, value);
		element = conv.convert(expr);
		text = realiser.realise(element).getRealisation();
		System.out.println(expr + " --> " + text);
		Assert.assertEquals("Conversion failed.", "?s is less than 1", text);

		// ?s <= value
		expr = new E_LessThanOrEqual(var, value);
		element = conv.convert(expr);
		text = realiser.realise(element).getRealisation();
		System.out.println(expr + " --> " + text);
		Assert.assertEquals("Conversion failed.", "?s is less than or equal to 1", text);

		/*
		 * date literals
		 */
		Calendar cal = Calendar.getInstance();
		cal.set(1999, 11, 20);
		value = NodeValue.makeDate(cal);
		String valueString = "December 20, 1999";

		// ?s = value
		expr = new E_Equals(var, value);
		element = conv.convert(expr);
		text = realiser.realise(element).getRealisation();
		System.out.println(expr + " --> " + text);
		Assert.assertEquals("Conversion failed.", "?s is on " + valueString, text);

		// ?s > value
		expr = new E_GreaterThan(var, value);
		element = conv.convert(expr);
		text = realiser.realise(element).getRealisation();
		System.out.println(expr + " --> " + text);
		Assert.assertEquals("Conversion failed.", "?s is after " + valueString, text);

		// ?s >= value
		expr = new E_GreaterThanOrEqual(var, value);
		element = conv.convert(expr);
		text = realiser.realise(element).getRealisation();
		System.out.println(expr + " --> " + text);
		Assert.assertEquals("Conversion failed.", "?s is after or on " + valueString, text);

		// ?s < value
		expr = new E_LessThan(var, value);
		element = conv.convert(expr);
		text = realiser.realise(element).getRealisation();
		System.out.println(expr + " --> " + text);
		Assert.assertEquals("Conversion failed.", "?s is before " + valueString, text);

		// ?s <= value
		expr = new E_LessThanOrEqual(var, value);
		element = conv.convert(expr);
		text = realiser.realise(element).getRealisation();
		System.out.println(expr + " --> " + text);
		Assert.assertEquals("Conversion failed.", "?s is before or on " + valueString, text);

		/*
		 * date period
		 */
		value = NodeValue.parse("\"1999-12\"^^xsd:gYearMonth");
		valueString = "December 1999";

		// ?s = value
		expr = new E_Equals(var, value);
		element = conv.convert(expr);
		text = realiser.realise(element).getRealisation();
		System.out.println(expr + " --> " + text);
		Assert.assertEquals("Conversion failed.", "?s is in " + valueString, text);

		// ?s >= value
		expr = new E_GreaterThanOrEqual(var, value);
		element = conv.convert(expr);
		text = realiser.realise(element).getRealisation();
		System.out.println(expr + " --> " + text);
		Assert.assertEquals("Conversion failed.", "?s is after or in " + valueString, text);

		// ?s <= value
		expr = new E_LessThanOrEqual(var, value);
		element = conv.convert(expr);
		text = realiser.realise(element).getRealisation();
		System.out.println(expr + " --> " + text);
		Assert.assertEquals("Conversion failed.", "?s is before or in " + valueString, text);
	}

}
