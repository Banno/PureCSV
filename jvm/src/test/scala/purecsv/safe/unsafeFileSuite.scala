/* Copyright 2015 Mario Pastorelli (pastorelli.mario@gmail.com)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package purecsv.safe

import java.io.CharArrayReader
import java.nio.file.Files

import purecsv.safe._
import purecsv.safe.tryutil._
import org.scalatest.{FunSuite, Matchers}
import purecsv.safe.converter.RawFieldsConverter
import shapeless.HNil

import scala.util.{Failure, Success, Try}


class SafeFileSuite extends FunSuite with Matchers {

  test("parse a csv string to a case class") {
    val file = readFileAsString("normal.csv")

    case class Person(first: String, second: String, email: String)
    CSVReader[Person].readCSVFromString(file, skipHeader = true) should be(List(Success(Person("Frank", "Bob", "frank.bob@foo.com"))))
  }

  test("convert to case class ignoring extra columns") {
    val file = readFileAsString("normal.csv")

    case class Person(first: String, last: String)

    //TODO: seems like it should be default behavior though
    implicit val deriveHNil = new RawFieldsConverter[HNil] {
      override def tryFrom(s: Seq[String]): Try[HNil] = Success(HNil)
      override def to(a: HNil): Seq[String] = Seq.empty
    }

    CSVReader[Person].readCSVFromString(file, skipHeader = true) should be(List(Success(Person("Frank", "Bob"))))
  }

  test("error when a CSV doesn't contain the right amount of fields for case class conversion") {
    val file = readFileAsString("normal.csv")

    case class Person(first: String, second: String, email: String, id: String)


    val failureMessages = CSVReader[Person].readCSVFromString(file, skipHeader = true) collect {
      case Failure(e: IllegalArgumentException) => e.getMessage
    }

    //TODO: should get message saying it was missing an id
    val expectedErrorMessage = " cannot be converter to a value of type class shapeless.$colon$colon"
    failureMessages should be(List(expectedErrorMessage))
  }


  def readFileAsString(fileName: String): String =
    scala.io.Source.fromURL(getClass.getResource(s"/csv/${fileName}")).mkString

  object Headers {
    val firstName = "First Name"
    val lastName = "Last"
    val email = "Email"
    val value = Seq(firstName, lastName, email)
  }

}
