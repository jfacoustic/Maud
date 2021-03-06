package ec.gp;

import ec.*;

import java.io.PrintWriter;

import ec.util.*;
import ec.EvolutionState;

/*
 * GPNode.java
 *
 * Created: Fri Aug 27 17:14:12 1999
 * By: Sean Luke
 */

/**
 * GPNode is a GPNodeParent which is the abstract superclass of
 * all GP function nodes in trees.  GPNode contains quite a few functions
 * for cloning subtrees in special ways, counting the number of nodes
 * in subtrees in special ways, and finding specific nodes in subtrees.
 *
 * GPNode's protoClone() method does not clone its children (it copies the
 * array, but that's it).  If you want to deep-clone a tree or subtree, you
 * should use one of the cloneReplacing(...) methods instead.
 *
 * <p>GPNodes contain a number of important items:
 * <ul><li>A <i>constraints</i> object which defines the name of the node,
 * its arity, and its type constraints. This
 * object is shared with all GPNodes of the same function name/arity/returntype/childtypes.
 * <li>A <i>parent</i>.  This is either another GPNode, or (if this node
 * is the root) a GPTree.
 * <li>Zero or more <i>children</i>, which are GPNodes.
 * <li>An argument position in its parent.
 * </ul>
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>nc</tt><br>
 <font size=-1>String</font></td>
 <td valign=top>(name of the node constraints for the GPNode)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 gp.node

 *
 * @author Sean Luke
 * @version 1.0
 */

public abstract class GPNode implements GPNodeParent {
  public static final String P_NODE = "node";
  public static final String P_NODECONSTRAINTS = "nc";
  public static final String GPNODEPRINTTAB = "    ";
  public static final int MAXPRINTBYTES = 40;

  public static final int NODESEARCH_ALL = 0;
  public static final int NODESEARCH_TERMINALS = 1;
  public static final int NODESEARCH_NONTERMINALS = 2;
  public static final int NODESEARCH_CUSTOM = 3;

  public static final int SITUATION_NEWIND = 0;
  public static final int SITUATION_MUTATION = 1;

  // beats me if Java compilers will take advantage of the int->byte shortening.
  // They may want everything aligned, in which case they may buffer the object
  // anyway, hope not!

  /** The GPNode's parent.  4 bytes.  :-(  But it really helps simplify breeding. */
  public GPNodeParent parent;
  public GPNode children[];
  /** The argument position of the child in its parent.
   This is a byte to save space (GPNode is the critical object space-wise) --
   besides, how often do you have 256 children? You can change this to a short
   or int easily if you absolutely need to.  It's possible to eliminate even
   this and have the child find itself in its parent, but that's an O(children[])
   operation, and probably not inlinable, so I figure a byte is okay. */
  public byte argposition;
  /** The GPNode's constraints.  This is a byte to save space -- how often do
   you have 256 different GPNodeConstraints?  Well, I guess it's not infeasible.
   You can increase4 this to an int without much trouble.  You typically
   shouldn't access the constraints through this variable -- use the constraints()
   method instead. */
  public byte constraints;

  // just in case Java's stupid enough to not move this out of the class, I
  // put it here with the other bytes.
  public static final char REPLACEMENT_CHAR = '@';


  /* Returns the GPNode's constraints.  A good JIT compiler should inline this. */
  public final GPNodeConstraints constraints() {
    return GPNodeConstraints.constraints[constraints];
  }

  /** The default base for GPNodes -- defined even though
   GPNode is abstract so you don't have to in subclasses. */
  public Parameter defaultBase() {
    return GPDefaults.base().push(P_NODE);
  }

  /** You ought to override this method to check to make sure that the
   constraints are valid as best you can tell.  Things you might
   check for:

   <ul>
   <li> children.length is correct
   <li> certain arguments in constraints.childtypes are
   swap-compatible with each other
   <li> constraints.returntype is swap-compatible with appropriate
   arguments in constraints.childtypes
   </ul>

   You can't check for everything, of course, but you might try some
   obvious checks for blunders.  The default version of this method
   is empty for now, but you should still call super.checkConstraints(state)
   just to be certain.

   The ultimate caller of this method must guarantee that he will eventually
   call state.output.exitIfErrors(), so you can freely use state.output.error
   instead of state.output.fatal(), which will help a lot.

   Warning: this method may get called more than once.
   */

  public void checkConstraints(final EvolutionState state,
                               final int tree,
                               final GPIndividual typicalIndividual,
                               final Parameter individualBase) {
  }

  /**
   Sets up a <i>prototypical</i> GPNode with those features all nodes of that
   prototype share, and nothing more.  So no filled-in children,
   no argposition, no parent.  Yet.

   This must be called <i>after</i> the GPTypes and GPNodeConstraints
   have been set up.  Presently they're set up in GPInitializer,
   which gets called before this does, so we're safe.

   You should override this if you need to load some special features on
   a per-function basis.  Note that base hangs off of a function set, so
   this method may get called for different instances in the same GPNode
   class if they're being set up as prototypes for different GPFunctionSets.

   If you absolutely need some global base, then you should use something
   hanging off of GPDefaults.base().

   The ultimate caller of this method must guarantee that he will eventually
   call state.output.exitIfErrors(), so you can freely use state.output.error
   instead of state.output.fatal(), which will help a lot.
   */

  public void setup(final EvolutionState state, final Parameter base) {
    Parameter def = defaultBase();

// determine my constraints -- at this point, the constraints should have been loaded.
    String s = state.parameters.getString(base.push(P_NODECONSTRAINTS),
            def.push(P_NODECONSTRAINTS));
    if (s == null)
      state.output.fatal("No node constraints are defined for the GPNode " +
              toStringForError(), base.push(P_NODECONSTRAINTS),
              def.push(P_NODECONSTRAINTS));
    else
      constraints = GPNodeConstraints.constraintsFor(s, state).constraintNumber;

// The number of children is determined by the constraints.  Though
// for some special versions of GPNode, we may have to enforce certain
// rules, checked in children versions of setup(...)

    children = new GPNode[constraints().childtypes.length];
  }

  /** Returns the argument type of the slot that I fit into in my parent.
   If I'm the root, returns the treetype of the GPTree. */
  public final GPType parentType() {
    if (parent instanceof GPNode)
      return ((GPNode) parent).constraints().childtypes[argposition];
    else // it's a tree root
      return ((GPTree) parent).constraints().treetype;
  }


  /** Returns true if I can swap into node's position. */

  public final boolean swapCompatibleWith(final GPNode node) {
// I'm atomically compatible with him; a fast check
    if (constraints().returntype == node.constraints().returntype)  // no need to check for compatibility
      return true;

// I'm set compatible with his parent's swap-position
    GPType type;
    if (node.parent instanceof GPNode)  // it's a GPNode
      type =
              ((GPNode) (node.parent)).constraints().childtypes[node.argposition];
    else // it's a tree root; I'm set compatible with the GPTree type
      type =
              ((GPTree) (node.parent)).constraints().treetype;

    return constraints().returntype.compatibleWith(type);
  }

  /** Returns the number of nodes, constrained by g.test(...)
   in the subtree for which this GPNode is root.  This might
   be sped up by caching the value.  O(n). */
  public int numNodes(final GPNodeGatherer g) {
    int s = 0;
    for (int x = 0; x < children.length; x++) s += children[x].numNodes(g);
    return s + (g.test(this) ? 1 : 0);
  }

  /** Returns the number of nodes, constrained by nodesearch,
   in the subtree for which this GPNode is root.
   This might be sped up by cacheing the value somehow.  O(n). */
  public int numNodes(final int nodesearch) {
    int s = 0;
    for (int x = 0; x < children.length; x++) s += children[x].numNodes(nodesearch);
    return s + ((nodesearch == NODESEARCH_ALL ||
            (nodesearch == NODESEARCH_TERMINALS && children.length == 0) ||
            (nodesearch == NODESEARCH_NONTERMINALS && children.length > 0)) ? 1 : 0);
  }

  /** Returns the depth of the tree, which is a value >= 1.  O(n). */
  public int depth() {
    int d = 0;
    int newdepth;
    for (int x = 0; x < children.length; x++) {
      newdepth = children[x].depth();
      if (newdepth > d) d = newdepth;
    }
    return d + 1;
  }

  /** Returns the depth at which I appear in the tree, which is a value >= 0. O(ln n) avg.*/
  public int atDepth() {
// -- new code, no need for recursion
    GPNodeParent cparent = parent;
    int count = 0;

    while (cparent != null && cparent instanceof GPNode) {
      count++;
      cparent = ((GPNode) (cparent)).parent;
    }
    return count;

    /* // -- old code
if (parent==null) return 0;
if (!(parent instanceof GPNode))  // found the root!
  return 0;
else return 1 + ((GPNode)parent).atDepth();
*/
  }

  /** Returns the p'th node, constrained by nodesearch,
   in the subtree for which this GPNode is root.
   Use numNodes(nodesearch) to determine the total number.  Or if
   you used numNodes(g), then when
   nodesearch == NODESEARCH_CUSTOM, g.test(...) is used
   as the constraining predicate.
   p ranges from 0 to this number minus 1. O(n). The
   resultant node is returned in <i>g</i>.*/
  public int nodeInPosition(int p, final GPNodeGatherer g, final int nodesearch) {

// am I of the type I'm looking for?
    if (nodesearch == NODESEARCH_ALL ||
            (nodesearch == NODESEARCH_TERMINALS && children.length == 0) ||
            (nodesearch == NODESEARCH_NONTERMINALS && children.length > 0) ||
            (nodesearch == NODESEARCH_CUSTOM && g.test(this))) {
      // is the count now at 0?  Is it me?
      if (p == 0) {
        g.node = this;
        return -1; // found it
      }
      // if it's not me, drop the count by 1
      else
        p--;
    }

// regardless, check my children if I've not returned by now
    for (int x = 0; x < children.length; x++) {
      p = children[x].nodeInPosition(p, g, nodesearch);
      if (p == -1) return -1; // found it
    }
    return p;
  }

  /** Returns the root ancestor of this node.  O(ln n) average case,
   O(n) worst case. */

  public GPNodeParent rootParent() {

// -- new code, no need for recursion
    GPNodeParent cparent = this;
    while (cparent != null && cparent instanceof GPNode)
      cparent = ((GPNode) (cparent)).parent;
    return cparent;


/* // -- old code
	if (parent==null) return null;
	if (!(parent instanceof GPNode))  // found the root!
	    return parent;
	return ((GPNode)parent).rootParent();
	*/
  }

  /** Returns true if the subtree rooted at this node contains subnode.  O(n). */
  public boolean contains(final GPNode subnode) {
    if (subnode == this) return true;
    for (int x = 0; x < children.length; x++)
      if (children[x].contains(subnode)) return true;
    return false;
  }


  /** Starts a node in a new life immediately after it has been cloned.
   The default version of this function does nothing.  The purpose of
   this function is to give ERCs a chance to set themselves to a new
   random value after they've been cloned from the prototype.
   You should not assume that the node is properly connected to other
   nodes in the tree at the point this method is called. */

  public void resetNode(final EvolutionState state, final int thread) {
  }

  /** A convenience function for identifying a GPNode in an error message */
  public String errorInfo() {
    return "GPNode " + toString() + " in the function set for tree " + ((GPTree) (rootParent())).treeNumber();
  }


  public Object protoClone() throws CloneNotSupportedException {
    GPNode obj = (GPNode) (super.clone());
    obj.children = new GPNode[children.length];
    return obj;
  }

  public final Object protoCloneSimple() {
    try {
      return protoClone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError();
    } // never happens
  }

  /** Deep-clones the tree rooted at this node, and returns the entire
   copied tree.  The result has everything set except for the root
   node's parent and argposition.*/

  public final GPNode cloneReplacing() throws CloneNotSupportedException {
    GPNode newnode = (GPNode) (protoClone());
    for (int x = 0; x < children.length; x++) {
      newnode.children[x] = (GPNode) (children[x].cloneReplacing());
      // if you think about it, the following CAN'T be implemented by
      // the children's clone method.  So it's set here.
      newnode.children[x].parent = newnode;
      newnode.children[x].argposition = (byte) x;
    }
    return newnode;
  }

  /** Deep-clones the tree rooted at this node, and returns the entire
   copied tree. The result has everything set except for the root
   node's parent and argposition.
   Does not throw CloneNotSupportedException. */

  public final GPNode cloneReplacingSimple() {
    try {
      return cloneReplacing();
    } catch (CloneNotSupportedException e) {
      throw new InternalError();
    } // never happens
  }


  /** Deep-clones the tree rooted at this node, and returns the entire
   copied tree.  If the node oldSubtree is located somewhere in this
   tree, then its subtree is replaced with a deep-cloned copy of
   newSubtree.  The result has everything set except for the root
   node's parent and argposition. */

  public final GPNode cloneReplacing(final GPNode newSubtree, final GPNode oldSubtree) throws CloneNotSupportedException {
    if (this == oldSubtree)
      return newSubtree.cloneReplacing();
    else {
      GPNode newnode = (GPNode) (protoClone());
      for (int x = 0; x < children.length; x++) {
        newnode.children[x] = (GPNode) (children[x].cloneReplacing(newSubtree, oldSubtree));
        // if you think about it, the following CAN'T be implemented by
        // the children's clone method.  So it's set here.
        newnode.children[x].parent = newnode;
        newnode.children[x].argposition = (byte) x;
      }
      return newnode;
    }
  }


  /** Deep-clones the tree rooted at this node, and returns the entire
   copied tree.  If the node oldSubtree is located somewhere in this
   tree, then its subtree is replaced with a deep-cloned copy of
   newSubtree.  The result has everything set except for the root
   node's parent and argposition. Does not throw
   CloneNotSupportedException. */

  public final GPNode cloneReplacingSimple(final GPNode newSubtree, final GPNode oldSubtree) {
    try {
      return cloneReplacing(newSubtree, oldSubtree);
    } catch (CloneNotSupportedException e) {
      throw new InternalError();
    } // never happens
  }

  /** Deep-clones the tree rooted at this node, and returns the entire
   copied tree.  If the node oldSubtree is located somewhere in this
   tree, then its subtree is replaced with
   newSubtree (<i>not</i> a copy of newSubtree).
   The result has everything set except for the root
   node's parent and argposition. */

  public final GPNode cloneReplacingNoSubclone(final GPNode newSubtree, final GPNode oldSubtree) throws CloneNotSupportedException {
    if (this == oldSubtree) {
      return newSubtree;
    } else {
      GPNode newnode = (GPNode) (protoClone());
      for (int x = 0; x < children.length; x++) {
        newnode.children[x] = (GPNode) (children[x].cloneReplacingNoSubclone(newSubtree, oldSubtree));
        // if you think about it, the following CAN'T be implemented by
        // the children's clone method.  So it's set here.
        newnode.children[x].parent = newnode;
        newnode.children[x].argposition = (byte) x;
      }
      return newnode;
    }
  }

  /** Deep-clones the tree rooted at this node, and returns the entire
   copied tree.  If the node oldSubtree is located somewhere in this
   tree, then its subtree is replaced with
   newSubtree (<i>not</i> a copy of newSubtree).
   The result has everything set except for the root
   node's parent and argposition. Does not throw
   CloneNotSupportedException. */

  public final GPNode cloneReplacingNoSubcloneSimple(final GPNode newSubtree, final GPNode oldSubtree) {
    try {
      return cloneReplacingNoSubclone(newSubtree, oldSubtree);
    } catch (CloneNotSupportedException e) {
      throw new InternalError();
    } // never happens
  }


  /** Deep-clones the tree rooted at this node, and returns the entire
   copied tree.  If a node in oldSubtrees is located somewhere in this
   tree, then its subtree is replaced with a deep-cloned copy of the
   subtree rooted at its equivalent number in
   newSubtrees.  The result has everything set except for the root
   node's parent and argposition. */

  public final GPNode cloneReplacing(final GPNode[] newSubtrees, final GPNode[] oldSubtrees) throws CloneNotSupportedException {
// am I a candidate?
    int candidate = -1;
    for (int x = 0; x < oldSubtrees.length; x++)
      if (this == oldSubtrees[x]) {
        candidate = x;
        break;
      }

    if (candidate >= 0)
      return newSubtrees[candidate].cloneReplacing(newSubtrees, oldSubtrees);
    else {
      GPNode newnode = (GPNode) (protoClone());
      for (int x = 0; x < children.length; x++) {
        newnode.children[x] = (GPNode) (children[x].cloneReplacing(newSubtrees, oldSubtrees));
        // if you think about it, the following CAN'T be implemented by
        // the children's clone method.  So it's set here.
        newnode.children[x].parent = newnode;
        newnode.children[x].argposition = (byte) x;
      }
      return newnode;
    }
  }

  /** Deep-clones the tree rooted at this node, and returns the entire
   copied tree.  If a node in oldSubtrees is located somewhere in this
   tree, then its subtree is replaced with a deep-cloned copy of the
   subtree rooted at its equivalent number in
   newSubtrees.  The result has everything set except for the root
   node's parent and argposition. Does not throw
   CloneNotSupportedException. */

  public final GPNode cloneReplacingSimple(final GPNode[] newSubtrees, final GPNode[] oldSubtrees) {
    try {
      return cloneReplacing(newSubtrees, oldSubtrees);
    } catch (CloneNotSupportedException e) {
      throw new InternalError();
    } // never happens
  }

  /** Clones a new subtree, but with the single node oldNode
   (which may or may not be in the subtree)
   replaced with a newNode (not a clone of newNode).
   These nodes should be
   type-compatible both in argument and return types, and should have
   the same number of arguments obviously.  This function will <i>not</i>
   check for this, and if they are not the result is undefined. */


  public final GPNode cloneReplacingAtomic(final GPNode newNode, final GPNode oldNode) throws CloneNotSupportedException {
    int numArgs;
    GPNode curnode;
    if (this == oldNode) {
      numArgs = Math.max(newNode.children.length, children.length);
      curnode = newNode;
    } else {
      numArgs = children.length;
      curnode = (GPNode) protoClone();
    }

// populate

    for (int x = 0; x < numArgs; x++) {
      curnode.children[x] = (GPNode) (children[x].cloneReplacingAtomic(newNode, oldNode));
      // if you think about it, the following CAN'T be implemented by
      // the children's clone method.  So it's set here.
      curnode.children[x].parent = curnode;
      curnode.children[x].argposition = (byte) x;
    }
    return curnode;
  }

  /** Clones a new subtree, but with the single node oldNode
   (which may or may not be
   in the subtree) replaced with newNode (not a clone of newNode).
   These nodes should be
   type-compatible both in argument and return types, and should have
   the same number of arguments obviously.  This function will <i>not</i>
   check for this, and if they are not the result is undefined. */

  public final GPNode cloneReplacingAtomicSimple(final GPNode newNode, final GPNode oldNode) {
    try {
      return cloneReplacingAtomic(newNode, oldNode);
    } catch (CloneNotSupportedException e) {
      throw new InternalError();
    } // never happens
  }


  /** Clones a new subtree, but with each node in oldNodes[] respectively
   (which may or may not be in the subtree) replaced with
   the equivalent
   nodes in newNodes[] (and not clones).
   The length of oldNodes[] and newNodes[] should
   be the same of course.  These nodes should be
   type-compatible both in argument and return types, and should have
   the same number of arguments obviously.  This function will <i>not</i>
   check for this, and if they are not the result is undefined. */


  public final GPNode cloneReplacingAtomic(final GPNode[] newNodes, final GPNode[] oldNodes) throws CloneNotSupportedException {
    int numArgs;
    GPNode curnode;
    int found = -1;

    for (int x = 0; x < newNodes.length; x++) {
      if (this == oldNodes[x]) found = x;
      break;
    }

    if (found > -1) {
      numArgs = Math.max(newNodes[found].children.length,
              children.length);
      curnode = newNodes[found];
    } else {
      numArgs = children.length;
      curnode = (GPNode) protoClone();
    }

// populate

    for (int x = 0; x < numArgs; x++) {
      curnode.children[x] = (GPNode) (children[x].cloneReplacingAtomic(newNodes, oldNodes));
      // if you think about it, the following CAN'T be implemented by
      // the children's clone method.  So it's set here.
      curnode.children[x].parent = curnode;
      curnode.children[x].argposition = (byte) x;
    }
    return curnode;
  }


  /** Clones a new subtree, but with each node in oldNodes[] respectively
   (which may or may not be in the subtree) replaced with
   the equivalent
   nodes in newNodes[] (and not clones of them).
   The length of oldNodes[] and newNodes[] should
   be the same of course.  These nodes should be
   type-compatible both in argument and return types, and should have
   the same number of arguments obviously.  This function will <i>not</i>
   check for this, and if they are not the result is undefined. */

  public final GPNode cloneReplacingAtomicSimple(final GPNode[] newNodes, final GPNode[] oldNodes) {
    try {
      return cloneReplacingAtomic(newNodes, oldNodes);
    } catch (CloneNotSupportedException e) {
      throw new InternalError();
    } // never happens
  }


  /** Returns a hashcode usually associated with all nodes that are
   equal to you (using nodeEquals(...)).  The default form
   of this method returns the hashcode of the node's class.
   ERCs in particular probably will want to override this method.
   */
  public int nodeHashCode() {
    return (this.getClass().hashCode());
  }

  /** Returns a hashcode associated with all the nodes in the tree.
   The default version adds the hash of the node plus its child
   trees, rotated one-off each time, which seems reasonable. */
  public int rootedTreeHashCode() {
    int hash = nodeHashCode();

    for (int x = 0; x < children.length; x++)
            // rotate hash and XOR
      hash =
              (hash << 1 | hash >>> 31) ^
              children[x].rootedTreeHashCode();
    return hash;
  }

  /** Returns true if I am the "genetically" same as this node, and our
   children arrays are the same length, though
   we may have different parents and children.  The default form
   of this method does a class comparison.  You may need
   to override this to perform special comparisons, if you're
   an ERC, ADF, or ADM for example. */
  public boolean nodeEquals(final GPNode node) {
    return (this.getClass().equals(node.getClass()) &&
            children.length == node.children.length);
  }

  /** Returns true if the two rooted trees are "genetically" equal, though
   they may have different parents.  O(n). */
  public boolean rootedTreeEquals(final GPNode node) {
    if (!nodeEquals(node)) return false;
    for (int x = 0; x < children.length; x++)
      if (!(children[x].rootedTreeEquals(node.children[x])))
        return false;
    return true;
  }

  /** Prints out a human-readable and Lisp-like atom for the node,
   and returns the number of bytes in the string that you sent
   to the log (use print(),
   not println()).  The default version gets the atom from
   toStringForHumans(). */
  public int printNodeForHumans(final EvolutionState state,
                                final int log,
                                final int verbosity) {
    String n = toStringForHumans();
    state.output.print(n, verbosity, log);
    return n.length();
  }


  /** Prints out a COMPUTER-readable and Lisp-like atom for the node, which
   is also suitable for readNode to read, and returns
   the number of bytes in the string that you sent to the log (use print(),
   not println()).  The default version gets the atom from toXRDcatString().
   O(1). */
  public int printNode(final EvolutionState state, final int log,
                       final int verbosity) {
    String n = toString();
    state.output.print(n, verbosity, log);
    return n.length();
  }


  /** Prints out a COMPUTER-readable and Lisp-like atom for the node, which
   is also suitable for readNode to read, and returns
   the number of bytes in the string that you sent to the log (use print(),
   not println()).  The default version gets the atom from toXRDcatString().
   O(1). */

  public int printNode(final EvolutionState state,
                       final PrintWriter writer) {
    String n = toString();
    writer.print(n);
    return n.length();
  }


  /** Returns a Lisp-like atom for the node which can be read in again by computer.
   If you need to encode an integer or a float or whatever for some reason
   (perhaps if it's an ERC), you should use the ec.util.Code library.  */

  public abstract String toString();

  /** Returns a Lisp-like atom for the node which is intended for human
   consumption, and not to be read in again.  The default version
   just calls toXRDcatString(). */

  public String toStringForHumans() {
    return toString();
  }

  /** Returns a description of the node that can make it easy to identify
   in error messages (by default, at least its name and the tree it's found in).
   It's okay if this is a reasonably expensive procedure -- it won't be called
   a lot.  */

  public String toStringForError() {
    GPTree rootp = (GPTree) rootParent();
    if (rootp != null) {
      int tnum = ((GPTree) (rootParent())).treeNumber();
      return toString() + (tnum == GPTree.NO_TREENUM ? "" : " in tree " + tnum);
    } else
      return toString();
  }

  /** Produces the LaTeX code for a LaTeX tree of the subtree rooted at this node, using the <tt>epic</tt>
   and <tt>fancybox</tt> packages, as described in sections 10.5.2 (page 307)
   and 10.1.3 (page 278) of <i>The LaTeX Companion</i>, respectively.  For this to
   work, the output of toXRDcatString() must not contain any weird latex characters, notably { or } or % or \,
   unless you know what you're doing. See the documentation for ec.gp.GPTree for information
   on how to take this code snippet and insert it into your LaTeX file. */

  public String makeLatexTree() {
    if (children.length == 0)
      return "\\gpbox{" + toString() + "}";

    String s = "\\begin{bundle}{\\gpbox{" + toString() + "}}";
    for (int x = 0; x < children.length; x++)
      s = s + "\\chunk{" + children[x].makeLatexTree() + "}";
    s = s + "\\end{bundle}";
    return s;
  }

  /** Prints out the tree on a single line, with no ending \n, in a fashion that can
   be read in later by computer. O(n).
   You should call this method with printbytes == 0. */

  public int printRootedTree(final EvolutionState state,
                             final int log, final int verbosity,
                             int printbytes) {
    if (children.length > 0) {
      state.output.print(" (", verbosity, log);
      printbytes += 2;
    } else {
      state.output.print(" ", verbosity, log);
      printbytes += 1;
    }

    printbytes += printNode(state, log, verbosity);

    for (int x = 0; x < children.length; x++)
      printbytes = children[x].printRootedTree(state, log, verbosity, printbytes);
    if (children.length > 0) {
      state.output.print(")", verbosity, log);
      printbytes += 1;
    }
    return printbytes;
  }


  /** Prints out the tree on a single line, with no ending \n, in a fashion that can
   be read in later by computer. O(n).  Returns the number of bytes printed.
   You should call this method with printbytes == 0. */

  public int printRootedTree(final EvolutionState state, final PrintWriter writer,
                             int printbytes) {
    if (children.length > 0) {
      writer.print(" (");
      printbytes += 2;
    } else {
      writer.print(" ");
      printbytes += 1;
    }

    printbytes += printNode(state, writer);

    for (int x = 0; x < children.length; x++)
      printbytes = children[x].printRootedTree(state, writer, printbytes);
    if (children.length > 0) {
      writer.print(")");
      printbytes += 1;
    }
    return printbytes;
  }


  /** Prints out the tree in a readable Lisp-like multi-line fashion. O(n).  You should call this method with tablevel and printbytes == 0.  No ending '\n' is printed.  */

  public int printRootedTreeForHumans(final EvolutionState state, final int log,
                                      final int verbosity,
                                      int tablevel, int printbytes) {
    if (printbytes > MAXPRINTBYTES) {
      state.output.print("\n", verbosity, log);
      tablevel++;
      printbytes = 0;
      for (int x = 0; x < tablevel; x++)
        state.output.print(GPNODEPRINTTAB, verbosity, log);
    }

    if (children.length > 0) {
      state.output.print(" (", verbosity, log);
      printbytes += 2;
    } else {
      state.output.print(" ", verbosity, log);
      printbytes += 1;
    }

    printbytes += printNodeForHumans(state, log, verbosity);

    for (int x = 0; x < children.length; x++)
      printbytes = children[x].printRootedTreeForHumans(state, log, verbosity, tablevel, printbytes);
    if (children.length > 0) {
      state.output.print(")", verbosity, log);
      printbytes += 1;
    }
    return printbytes;
  }


  /** Reads the node symbol,
   advancing the DecodeReturn to the first character in the string
   beyond the node symbol, and returns a new, empty GPNode of the
   appropriate class representing that symbol, else null if the
   node symbol is not of the correct type for your GPNode class. You may
   assume that initial whitespace has been eliminated.  Generally should
   be case-SENSITIVE, unlike in Lisp.  The default
   version usually works for "simple" function names, that is, not ERCs
   or other stuff where you have to encode the symbol. */

  public GPNode readNode(DecodeReturn dret) throws CloneNotSupportedException {
    int len = dret.data.length();

// get my name
    String str2 = toString();
    int len2 = str2.length();

    if (dret.pos + len2 > len)  // uh oh, not enough space
      return null;

// check it out
    for (int x = 0; x < len2; x++)
      if (dret.data.charAt(dret.pos + x) != str2.charAt(x))
        return null;

// looks good!  Check to make sure that
// the symbol's all there is
    if (dret.data.length() > dret.pos + len2) {
      char c = dret.data.charAt(dret.pos + len2);
      if (!Character.isWhitespace(c) &&
              c != ')' && c != '(') // uh oh
        return null;
    }

// we're happy!
    dret.pos += len2;
    return (GPNode) protoClone();
  }


  /** Reads the node and its children from the form printed out by printRootedTree. */

  public static GPNode readRootedTree(int linenumber,
                                      DecodeReturn dret,
                                      GPType expectedType,
                                      GPFunctionSet set,
                                      GPNodeParent parent,
                                      int argposition,
                                      EvolutionState state) throws CloneNotSupportedException {
// eliminate whitespace if any
    boolean isTerminal = true;
    int len = dret.data.length();
    for (; dret.pos < len &&
            Character.isWhitespace(dret.data.charAt(dret.pos)); dret.pos++)
      ;

// if I'm out of space, complain

    if (dret.pos >= len)
      state.output.fatal("Reading line " + linenumber + ": " + "Premature end of tree structure -- did you forget a close-parenthesis?\nThe tree was" + dret.data);

// if I've found a ')', complain
    if (dret.data.charAt(dret.pos) == ')') {
      StringBuffer sb = new StringBuffer(dret.data);
      sb.setCharAt(dret.pos, REPLACEMENT_CHAR);
      dret.data = sb.toString();
      state.output.fatal("Reading line " + linenumber + ": " + "Premature ')' which I have replaced with a '" + REPLACEMENT_CHAR + "', in tree:\n" + dret.data);
    }

// determine if I'm a terminal or not
    if (dret.data.charAt(dret.pos) == '(') {
      isTerminal = false;
      dret.pos++;
      // strip following whitespace
      for (; dret.pos < len &&
              Character.isWhitespace(dret.data.charAt(dret.pos)); dret.pos++)
        ;
    }

// check again if I'm out of space

    if (dret.pos >= len)
      state.output.fatal("Reading line " + linenumber + ": " + "Premature end of tree structure -- did you forget a close-parenthesis?\nThe tree was" + dret.data);

// check again if I found a ')'
    if (dret.data.charAt(dret.pos) == ')') {
      StringBuffer sb = new StringBuffer(dret.data);
      sb.setCharAt(dret.pos, REPLACEMENT_CHAR);
      dret.data = sb.toString();
      state.output.fatal("Reading line " + linenumber + ": " + "Premature ')' which I have replaced with a '" + REPLACEMENT_CHAR + "', in tree:\n" + dret.data);
    }


// find that node!
    GPFuncInfo[] gpfi = isTerminal ?
            set.terminals[expectedType.type] :
            set.nonterminals[expectedType.type];

    GPNode node = null;
    for (int x = 0; x < gpfi.length; x++)
      if ((node = gpfi[x].node.readNode(dret)) != null) break;

// did I find one?

    if (node == null) {
      if (dret.pos != 0) {
        StringBuffer sb = new StringBuffer(dret.data);
        sb.setCharAt(dret.pos, REPLACEMENT_CHAR);
        dret.data = sb.toString();
      } else
        dret.data = "" + REPLACEMENT_CHAR + dret.data;
      state.output.fatal("Reading line " + linenumber + ": " + "I came across a symbol which I could not match up with a type-valid node.\nI have replaced the position immediately before the node in question with a '" + REPLACEMENT_CHAR + "':\n" + dret.data);
    }

    node.parent = parent;
    node.argposition = (byte) argposition;

// do its children
    for (int x = 0; x < node.children.length; x++)
      node.children[x] = readRootedTree(linenumber, dret, node.constraints().childtypes[x], set, node, x, state);

// if I'm not a terminal, look for a ')'

    if (!isTerminal) {
      // clear whitespace
      for (; dret.pos < len &&
              Character.isWhitespace(dret.data.charAt(dret.pos)); dret.pos++)
        ;

      if (dret.pos >= len)
        state.output.fatal("Reading line " + linenumber + ": " + "Premature end of tree structure -- did you forget a close-parenthesis?\nThe tree was" + dret.data);

      if (dret.data.charAt(dret.pos) != ')') {
        if (dret.pos != 0) {
          StringBuffer sb = new StringBuffer(dret.data);
          sb.setCharAt(dret.pos, REPLACEMENT_CHAR);
          dret.data = sb.toString();
        } else
          dret.data = "" + REPLACEMENT_CHAR + dret.data;
        state.output.fatal("Reading line " + linenumber + ": " + "A nonterminal node has too many arguments.  I have put a '" +
                REPLACEMENT_CHAR + "' just before the offending argument.\n" + dret.data);
      } else
        dret.pos++;  // get rid of the ')'
    }

// return the node
    return node;
  }


  /** Evaluates the node with the given thread, state, individual, problem, and stack.
   Your random number generator will be state.random[thread].
   The node should, as appropriate, evaluate child nodes with these same items
   passed to eval(...).

   <p>About <b>input</b>: <tt>input</tt> is special; it is how data is passed between
   parent and child nodes.  If children "receive" data from their parent node when
   it evaluates them, they should receive this data stored in <tt>input</tt>.
   If (more likely) the parent "receives" results from its children, it should
   pass them an <tt>input</tt> object, which they'll fill out, then it should
   check this object for the returned value.

   <p>A tree is typically evaluated by dropping a GPData into the root.  When the
   root returns, the resultant <tt>input</tt> should hold the return value.

   <p>In general, you should not be creating new GPDatas.
   If you think about it, in most conditions (excepting ADFs and ADMs) you
   can use and reuse <tt>input</tt> for most communications purposes between
   parents and children.

   <p>So, let's say that your GPNode function implements the boolean AND function,
   and expects its children to return return boolean values (as it does itself).
   You've implemented your GPData subclass to be, uh, <b>BooleanData</b>, which
   looks like

   * <tt><pre>public class BooleanData extends GPData
   *    {
   *    public boolean result;
   *    public GPData copyTo(GPData gpd)
   *      {
   *      ((BooleanData)gpd).result = result;
   *      }
   *    }</pre></tt>

   <p>...so, you might implement your eval(...) function as follows:

   * <tt><pre>public void eval(final EvolutionState state,
   *                     final int thread,
   *                     final GPData input,
   *                     final ADFStack stack,
   *                     final GPIndividual individual,
   *                     final Problem problem
   *    {
   *    BooleanData dat = (BooleanData)input;
   *    boolean x;
   *
   *    // evaluate the first child
   *    children[0].eval(state,thread,input,stack,individual,problem);
   *
   *    // store away its result
   *    x = dat.result;
   *
   *    // evaluate the second child
   *    children[1].eval(state,thread,input,stack,individual,problem);
   *
   *    // return (in input) the result of the two ANDed
   *
   *    dat.result = dat.result && x;
   *    return;
   *    }
   </pre></tt>
   */

  public abstract void eval(final EvolutionState state,
                            final int thread,
                            final GPData input,
                            final ADFStack stack,
                            final GPIndividual individual,
                            final Problem problem);
}

