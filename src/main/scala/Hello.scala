@main def helloScalaMatsuri2024(): Unit =

  final case class Speaker(
     name: String,
     company: String,
     role: Set[String]
  )

  val speaker = Speaker(
    name = "Takato Horikoshi",
    company = "Funds",
    role = Set(
      "Software Developer",
      "Engineering Manager"
    )
  )

  def greeting(speaker: Speaker): String = {
    s"""Hello, I'm ${speaker.name}.
       |I'm a ${speaker.role.mkString(" / ")} at ${speaker.company}."""
      .stripMargin
  }

  print(greeting(speaker))
