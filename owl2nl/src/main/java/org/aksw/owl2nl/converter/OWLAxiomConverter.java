package org.aksw.owl2nl.converter;

import java.util.List;
import java.util.Set;

import org.aksw.owl2nl.data.IInput;
import org.aksw.owl2nl.data.OWL2NLInput;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLRule;

import simplenlg.features.Feature;
import simplenlg.framework.NLGElement;
import simplenlg.framework.NLGFactory;
import simplenlg.lexicon.Lexicon;
import simplenlg.phrasespec.SPhraseSpec;
import simplenlg.realiser.english.Realiser;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * Converts OWL logical axioms into natural language.
 *
 * @author Lorenz Buehmann
 * @author Rene Speck
 */
public class OWLAxiomConverter implements OWLAxiomVisitor {

  private static final Logger LOG = LogManager.getLogger(OWLAxiomConverter.class);

  private final NLGFactory nlgFactory;
  private final Realiser realiser;

  private final OWLClassExpressionConverter ceConverter;
  private final OWLPropertyExpressionConverter peConverter;

  private final OWLDataFactory df = new OWLDataFactoryImpl();

  private String nl = null;

  /**
   * OWLAxiomConverter class constructor.
   *
   * @param input
   */
  public OWLAxiomConverter(final IInput input) {
    nlgFactory = new NLGFactory(input.getLexicon());
    realiser = new Realiser(input.getLexicon());

    ceConverter = new OWLClassExpressionConverter(input);
    peConverter = new OWLPropertyExpressionConverter(input);
  }

  /**
   * OWLAxiomConverter class constructor.
   *
   * @param lexicon
   */
  public OWLAxiomConverter(final Lexicon lexicon) {
    this(new OWL2NLInput().setLexicon(lexicon));
  }

  /**
   * OWLAxiomConverter class constructor.
   *
   */
  public OWLAxiomConverter() {
    this(Lexicon.getDefaultLexicon());
  }

  /**
   * Converts the OWL axiom into natural language. Only logical axioms are supported, i.e.
   * declaration axioms and annotation axioms are not converted and <code>null</code> will be
   * returned instead.
   *
   * @param axiom the OWL axiom
   * @return the natural language expression
   */
  public String convert(final OWLAxiom axiom) {

    reset();

    if (axiom.isLogicalAxiom()) {
      axiom.accept(this);
      return nl;
    }
    return null;
  }

  private void reset() {
    nl = null;
  }

  // #########################################################
  // ################# OWLAxiomVisitor ################
  // #########################################################

  /**
   * A subclass axiom SubClassOf(CE1 CE2) states that the class expression CE1 is a subclass of the
   * class expression CE2<br>
   * <br>
   * Example:<br>
   * <br>
   * class expression: SubClassOf(a:Child a:Person)<br>
   * <br>
   * converted: Each child is a person.
   */
  @Override
  public void visit(final OWLSubClassOfAxiom axiom) {

    LOG.debug("Converting SubClassOf axiom: {}", axiom);

    final OWLClassExpression subClass = axiom.getSubClass();
    final OWLClassExpression superClass = axiom.getSuperClass();

    final NLGElement subClassElement = ceConverter.asNLGElement(subClass, true);
    final NLGElement superClassElement = ceConverter.asNLGElement(superClass);

    final SPhraseSpec clause = nlgFactory.createClause(subClassElement, "be", superClassElement);
    superClassElement.setFeature(Feature.COMPLEMENTISER, null);

    realise(clause);
  }

  private void realise(final SPhraseSpec clause) {
    if (nl == null) {
      nl = sPhraseSpecToString(clause);
    } else {
      nl = nl.concat(sPhraseSpecToString(clause));
    }
  }

  /**
   * Capitalizes the first character and concats a point and space at the end of the clause.
   *
   * @param clause
   * @return sentence
   */
  private String sPhraseSpecToString(final SPhraseSpec clause) {
    return StringUtils.capitalize(realiser.realise(clause).toString()).concat(". ");
  }

  /**
   * An equivalent classes axiom EquivalentClasses(CE1 ... CEn) states that all of the class
   * expressions CEi, 1 ≤ i ≤ n, are semantically equivalent to each other.<br>
   * <br>
   * Example:<br>
   * <br>
   * class expression: EquivalentClasses(a:Boy ObjectIntersectionOf(a:Child a:Man))<br>
   * <br>
   * converted: A boy is a male child.<br>
   * <br>
   * We rewrite EquivalentClasses(CE1 CE2) as SubClassOf(CE1 CE2) and SubClassOf(CE2 CE1)
   */
  @Override
  public void visit(final OWLEquivalentClassesAxiom axiom) {
    final List<OWLClassExpression> classExpressions = axiom.getClassExpressionsAsList();
    LOG.debug("Converting EquivalentClasses axiom: {}, {}", axiom, classExpressions);

    for (int i = 0; i < classExpressions.size(); i++) {
      for (int j = i + 1; j < classExpressions.size(); j++) {
        df.getOWLSubClassOfAxiom(//
            classExpressions.get(i), classExpressions.get(j)//
        ).accept(this);
      }
    }
  }

  /**
   * A disjoint classes axiom DisjointClasses(CE1 ... CEn) states that all of the class expressions
   * CEi, 1 ≤ i ≤ n, are pairwise disjoint; that is, no individual can be at the same time an
   * instance of both CEi and CEj for i ≠ j.<br>
   * <br>
   * Example:<br>
   * <br>
   * class expression: DisjointClasses(a:Boy a:Girl)<br>
   * <br>
   * converted: Nothing can be both a boy and a girl.<br>
   * <br>
   * We rewrite DisjointClasses(CE1 ... CEn) as SubClassOf(CEi, ObjectComplementOf(CEj)) for each
   * subset {CEi,CEj} with i != j
   */
  @Override
  public void visit(final OWLDisjointClassesAxiom axiom) {
    final List<OWLClassExpression> classExpressions = axiom.getClassExpressionsAsList();
    LOG.debug("Converting DisjointClasses axiom: {}, {}", axiom, classExpressions);

    for (int i = 0; i < classExpressions.size(); i++) {
      for (int j = i + 1; j < classExpressions.size(); j++) {
        df.getOWLSubClassOfAxiom(//
            classExpressions.get(i), df.getOWLObjectComplementOf(classExpressions.get(j))//
        ).accept(this);
      }
    }
  }

  /**
   * DisjointUnion class expression allows one to define a class as a disjoint union of several
   * class expressions and thus to express covering constraints.<br>
   * <br>
   * Example:<br>
   * <br>
   * class expression: DisjointUnion(a:Child a:Boy a:Girl)<br>
   * <br>
   * converted: Each child is either a boy or a girl, each boy is a child, each girl is a child, and
   * nothing can be both a boy and a girl.<br>
   * <br>
   * <br>
   * We rewrite DisjointUnion(C , CE1 ... CEn) as EquivalentClasses(C ,ObjectUnionOf(CE1 ... CEn ))
   * and DisjointClasses(CE1 ... CEn)
   */
  @Override
  public void visit(final OWLDisjointUnionAxiom axiom) {
    // verb.addFrontModifier("either");
    final Set<OWLClassExpression> classExpressions = axiom.getClassExpressions();
    LOG.debug("Converting DisjointUnion axiom: {}, {}", axiom, classExpressions);
    df.getOWLEquivalentClassesAxiom(///
        axiom.getOWLClass(), df.getOWLObjectUnionOf(classExpressions)//
    ).accept(this);
    df.getOWLDisjointClassesAxiom(classExpressions).accept(this);
  }

  // #########################################################
  // ################# object property axioms ################
  // #########################################################

  /**
   * SubObjectPropertyOf( a:hasDog a:hasPet ) Having a dog implies having a pet.
   */
  @Override
  public void visit(final OWLSubObjectPropertyOfAxiom axiom) {
    LOG.info("OWLSubObjectPropertyOfAxiom");

    final OWLObjectPropertyExpression superProperty = axiom.getSuperProperty();
    final OWLObjectPropertyExpression subProperty = axiom.getSubProperty();

    final NLGElement subPropertyElement = peConverter.asNLGElement(subProperty, true);

    peConverter.asNLGElement(subProperty, true);

    // convert the super property
    final NLGElement superPropertyElement = peConverter.asNLGElement(superProperty);

    final SPhraseSpec clause =
        nlgFactory.createClause(subPropertyElement, "imply", superPropertyElement);
    superPropertyElement.setFeature(Feature.COMPLEMENTISER, null);

    nl = realiser.realise(clause).toString();
  }

  @Override
  public void visit(final OWLEquivalentObjectPropertiesAxiom axiom) {
    // SubObjectPropertyOf( OPE1 OPE2 )
    // SubObjectPropertyOf( OPE2 OPE1 )
    axiom.asSubObjectPropertyOfAxioms().forEach(p -> p.accept(this));
  }

  // TODO: implement me
  @Override
  public void visit(final OWLDisjointObjectPropertiesAxiom axiom) {}

  @Override
  public void visit(final OWLObjectPropertyDomainAxiom axiom) {
    axiom.asOWLSubClassOfAxiom()//
        // SubClassOf( ObjectSomeValuesFrom( OPE owl:Thing ) CE )
        .accept(this);
  }

  @Override
  public void visit(final OWLObjectPropertyRangeAxiom axiom) {
    axiom.asOWLSubClassOfAxiom()//
        // SubClassOf( owl:Thing ObjectAllValuesFrom( OPE CE ) )
        .accept(this);
  }

  @Override
  public void visit(final OWLInverseObjectPropertiesAxiom axiom) {
    // EquivalentObjectProperties( OPE1 ObjectInverseOf( OPE2 ) )
    df.getOWLEquivalentObjectPropertiesAxiom(//
        axiom.getFirstProperty(), axiom.getSecondProperty().getInverseProperty()//
    ).accept(this);
  }

  @Override
  public void visit(final OWLFunctionalObjectPropertyAxiom axiom) {
    axiom.asOWLSubClassOfAxiom()//
        // SubClassOf( owl:Thing ObjectMaxCardinality( 1 OPE ) )
        .accept(this);
  }

  // TODO: implement me
  @Override
  public void visit(final OWLAsymmetricObjectPropertyAxiom axiom) {

  }

  @Override
  public void visit(final OWLReflexiveObjectPropertyAxiom axiom) {
    axiom.asOWLSubClassOfAxiom()//
        // SubClassOf( owl:Thing ObjectHasSelf( OPE ) )
        .accept(this);
  }

  @Override
  public void visit(final OWLSymmetricObjectPropertyAxiom axiom) {
    // SubObjectPropertyOf( OPE ObjectInverseOf( OPE ) )
    df.getOWLSubObjectPropertyOfAxiom(//
        axiom.getProperty(), axiom.getProperty().getInverseProperty()//
    ).accept(this);
  }

  // TODO: implement me
  @Override
  public void visit(final OWLTransitiveObjectPropertyAxiom axiom) {
    // SubObjectPropertyOf( ObjectPropertyChain( OPE OPE ) OPE )
    // df.getOWLSubObjectPropertyOfAxiom(//
    //
    // ).accept(this);
  }

  @Override
  public void visit(final OWLIrreflexiveObjectPropertyAxiom axiom) {
    axiom.asOWLSubClassOfAxiom()//
        // SubClassOf( ObjectHasSelf( OPE ) owl:Nothing )
        .accept(this);
  }

  @Override
  public void visit(final OWLInverseFunctionalObjectPropertyAxiom axiom) {
    axiom.asOWLSubClassOfAxiom()//
        // SubClassOf( owl:Thing ObjectMaxCardinality( 1 ObjectInverseOf( OPE ) ) )
        .accept(this);
  }

  // #########################################################
  // ################# data property axioms ##################
  // #########################################################

  /**
   * A data subproperty axiom SubDataPropertyOf( DPE1 DPE2 ) states that the data property
   * expression DPE1 is a subproperty of the data property expression DPE2 — that is, if an
   * individual x is connected by DPE1 to a literal y, then x is connected by DPE2 to y as well.
   * Example:<br>
   * <br>
   * SubDataPropertyOf( a:hasLastName a:hasName ) <br>
   * A last name of someone is his/her name as well.
   */
  // TODO: implement me
  @Override
  public void visit(final OWLSubDataPropertyOfAxiom axiom) {

    final OWLDataPropertyExpression dpe1 = axiom.getSubProperty();
    final OWLDataPropertyExpression dpe2 = axiom.getSuperProperty();

    LOG.info("{} {} ", dpe1, dpe2);
  }

  @Override
  public void visit(final OWLEquivalentDataPropertiesAxiom axiom) {
    // SubDataPropertyOf( DPE1 DPE2 )
    // SubDataPropertyOf( DPE2 DPE1 )
    axiom.asSubDataPropertyOfAxioms().forEach(p -> p.accept(this));
  }

  /**
   * A disjoint data properties axiom DisjointDataProperties( DPE1 ... DPEn ) states that all of the
   * data property expressions DPEi, 1 ≤ i ≤ n, are pairwise disjoint; that is, no individual x can
   * be connected to a literal y by both DPEi and DPEj for i ≠ j.<br>
   * <br>
   * Example:<br>
   * DisjointDataProperties( a:hasName a:hasAddress ) <br>
   * Someone's name must be different from his address.
   */
  // TODO: implement me
  @Override
  public void visit(final OWLDisjointDataPropertiesAxiom axiom) {

  }

  @Override
  public void visit(final OWLDataPropertyDomainAxiom axiom) {
    axiom.asOWLSubClassOfAxiom().accept(this);
  }

  @Override
  public void visit(final OWLDataPropertyRangeAxiom axiom) {
    axiom.asOWLSubClassOfAxiom().accept(this);
  }

  @Override
  public void visit(final OWLFunctionalDataPropertyAxiom axiom) {
    axiom.asOWLSubClassOfAxiom().accept(this);
  }

  // #########################################################
  // ################# other logical axioms ##################
  // #########################################################

  // TODO: implement me
  @Override
  public void visit(final OWLSubPropertyChainOfAxiom axiom) {
    LOG.info(axiom.getPropertyChain());
  }

  // TODO: implement me
  @Override
  public void visit(final OWLHasKeyAxiom axiom) {
    // is uniquely identified by its
    // is uniquely identified by their
    nl = "";
    LOG.info(axiom);
  }

  // TODO: implement me
  @Override
  public void visit(final OWLDatatypeDefinitionAxiom axiom) {

    // final OWLDatatype datatype = axiom.getDatatype();
    final OWLDataRange datarange = axiom.getDataRange();

    // final NLGElement e = ceConverter.visit(datatype);
    final SPhraseSpec clause = nlgFactory.createClause(//
        "The datatype x", //
        "be", //
        datarange.accept(ceConverter)//
    );

    realise(clause);
    LOG.info("nl:{}", nl);
  }

  // TODO: implement me
  @Override
  public void visit(final SWRLRule axiom) {

    final Set<SWRLAtom> head = axiom.getHead();
    final Set<SWRLAtom> body = axiom.getBody();

    LOG.info("head:{}\nbody:{}", head, body);

  }

  // #########################################################
  // ################# individual axioms #####################
  // #########################################################

  @Override
  public void visit(final OWLClassAssertionAxiom axiom) {}

  @Override
  public void visit(final OWLObjectPropertyAssertionAxiom axiom) {}

  @Override
  public void visit(final OWLDataPropertyAssertionAxiom axiom) {}

  @Override
  public void visit(final OWLNegativeObjectPropertyAssertionAxiom axiom) {}

  @Override
  public void visit(final OWLNegativeDataPropertyAssertionAxiom axiom) {}

  @Override
  public void visit(final OWLDifferentIndividualsAxiom axiom) {}

  @Override
  public void visit(final OWLSameIndividualAxiom axiom) {}
  // #########################################################
  // ################# non-logical axioms ####################
  // #########################################################

  @Override
  public void visit(final OWLAnnotationAssertionAxiom axiom) {}

  @Override
  public void visit(final OWLSubAnnotationPropertyOfAxiom axiom) {}

  @Override
  public void visit(final OWLAnnotationPropertyDomainAxiom axiom) {}

  @Override
  public void visit(final OWLAnnotationPropertyRangeAxiom axiom) {}

  @Override
  public void visit(final OWLDeclarationAxiom axiom) {}
}