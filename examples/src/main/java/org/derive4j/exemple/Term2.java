/*
 * Copyright (c) 2015, Jean-Baptiste Giraudeau <jb@giraudeau.info>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.derive4j.exemple;

import org.derive4j.Data;
import org.derive4j.FieldNames;

import java.util.function.BiFunction;
import java.util.function.Function;

import static java.lang.System.out;
import static org.derive4j.exemple.Term2s.*;

// Implementation of a pseudo-GADT in Java, translating the examples from
// http://www.cs.ox.ac.uk/ralf.hinze/publications/With.pdf
// The technique presented below is, in fact, just an encoding of a normal Algebraic Data Type
// using a variation of the visitor pattern + the application of the Yoneda lemma to make it
// isomorphic to the targeted 'GADT'.
// Highlights:
// -> no cast and no subtyping.
// -> all of the eval function logic is static and not scattered all around Term subclasses.
@Data
public abstract class Term2<T> {
  Term2() {
  }

  public static <T> T eval(final Term2<T> term) {

    Function<Term2<T>, T> eval = Term2s.<T>cases().
        Zero(__                    -> __.__(0)).
        Succ((t, __)               -> __.__(eval(t) + 1)).
        Pred((t, __)               -> __.__(eval(t) - 1)).
        IsZero((t, __)             -> __.__(eval(t) == 0)).
        If((cond, then, otherwise) -> eval(cond) ? eval(then) : eval(otherwise));

    return eval.apply(term);
  }

  public static void main(final String[] args) {
    Term2<Integer> one = Succ(Zero());
    out.println(eval(one)); // "1"
    out.println(eval(IsZero(one))); // "false"
    // IsZero(IsZero(one)); // does not compile:
    // "The method IsZero(Term<Integer>) in the type Term<T> is not
    // applicable for the arguments (Term<Boolean>)"
    out.println(eval(If(IsZero(one), Zero(), one))); // "1"
    Term2<Boolean> True = IsZero(Zero());
    Term2<Boolean> False = IsZero(one);
    out.println(eval(If(True, True, False))); // "true"
    // out.println(prettyPrint(If(True, True, False), 0)); // "if IsZero(0)
    //  then IsZero(0)
    //  else IsZero(Succ(0))"
  }

  public abstract <X> X match(@FieldNames("__") Function<F<Integer, T>, X> Zero,
                              @FieldNames({"term", "__"}) BiFunction<Term2<Integer>, F<Integer, T>, X> Succ,
                              @FieldNames({"term", "__"}) BiFunction<Term2<Integer>, F<Integer, T>, X> Pred,
                              @FieldNames({"term", "__"}) BiFunction<Term2<Integer>, F<Boolean, T>, X> IsZero,
                              @FieldNames({"cond", "then", "otherwise"}) TriFunction<Term2<Boolean>, Term2<T>, Term2<T>, X> If);

  public interface F<A, B> {// Could be java.util.function.Function,

    //used only for the visualy lighter apply method.
    B __(A a);
  }

  @Override
  public abstract boolean equals(Object obj);

  @Override
  public abstract int hashCode();

  @Override
  public abstract String toString();

  public interface TriFunction<A, B, C, R> {// Could be java.util.function.Function,

    //used only for the visualy lighter apply method.
    R appy(A a, B b, C c);
  }
}
