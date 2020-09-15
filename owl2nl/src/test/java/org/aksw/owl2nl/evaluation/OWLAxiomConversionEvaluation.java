package org.aksw.owl2nl.evaluation;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.aksw.owl2nl.OWLAxiomConverter;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntax;
import org.dllearner.utilities.owl.ManchesterOWLSyntaxOWLObjectRendererImplExt;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import simplenlg.lexicon.Lexicon;

/**
 * @author Lorenz Buehmann created on 11/4/15
 */
public class OWLAxiomConversionEvaluation {

  // static String ontologyURL =
  // "https://raw.githubusercontent.com/pezra/pretty-printer/master/Jenna-2.6.3/testing/ontology/bugs/koala.owl";
  // static String ontologyURL = "http://protege.cim3.net/file/pub/ontologies/travel/travel.owl";

  // static String ontologyURL = "https://protege.stanford.edu/ontologies/travel.owl";

  static File ontologyURL = Paths.get(//
      // "/media/store/SVN/UPB/projects/2019/RAKI/Usecases/Siemens Usecase Skill
      // Learning/Ontologies/IndustryOntologies/IndustrialDFOntology.owl"
      "/home/rspeck/Desktop/Usecases/Siemens Usecase Skill Learning/Ontologies/PPP_Ontologies/Process.owl"
  //
  ).toFile();

  public static void main(final String[] args) throws Exception {
    final OWLObjectRenderer renderer = new ManchesterOWLSyntaxOWLObjectRendererImplExt();

    final OWLOntologyManager man = OWLManager.createOWLOntologyManager();
    final OWLOntology ontology = man.loadOntology(IRI.create(ontologyURL));

    final List<List<String>> data = new ArrayList<>();

    final OWLAxiomConverter converter = new OWLAxiomConverter(Lexicon.getDefaultLexicon(), null);
    int i = 1;
    for (final OWLAxiom axiom : ontology.getAxioms()) {
      final String s = converter.convert(axiom);

      if (s != null) {

        System.out.println(axiom);
        System.out.println(s);
        System.out.println("------");

        final List<String> rowData = new ArrayList<>();
        rowData.add(String.valueOf(i++));

        String renderedAxiom = renderer.render(axiom);
        for (final ManchesterOWLSyntax keyword : ManchesterOWLSyntax.values()) {
          if (keyword.isAxiomKeyword() || keyword.isClassExpressionConnectiveKeyword()
              || keyword.isClassExpressionQuantiferKeyword()) {
            final String regex =
                "\\s?(" + keyword.keyword() + "|" + keyword.toString() + ")(\\s|:)";
            renderedAxiom = renderedAxiom.replaceAll(regex, " <b>" + keyword.keyword() + "</b> ");
          }
        }
        rowData.add(renderedAxiom);
        rowData.add(s);

        data.add(rowData);
      }
    }

    final String htmlTable =
        HTMLTableGenerator.generateHTMLTable(Lists.newArrayList("ID", "Axiom", "NL"), data);
    final File file = new File("/tmp/axiomConversionResults.html");
    System.out.println(file.getPath());
    Files.write(htmlTable, file, Charsets.UTF_8);
  }
}
