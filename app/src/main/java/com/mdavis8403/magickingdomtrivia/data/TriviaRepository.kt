package com.mdavis8403.magickingdomtrivia.data

class TriviaRepository {
    private val categories = listOf(
        TriviaCategory(
            id = "lands",
            title = "Magic Lands",
            description = "Adventureland to Tomorrowland, test how well you know the park map.",
            accentColor = 0xFF2AC7D2,
        ),
        TriviaCategory(
            id = "rides",
            title = "Attractions",
            description = "Classic rides, hidden details, and queue-story surprises.",
            accentColor = 0xFFF6B84B,
        ),
        TriviaCategory(
            id = "characters",
            title = "Characters",
            description = "Sidekicks, royalty, and the residents who make the park feel alive.",
            accentColor = 0xFFFE6A7A,
        ),
        TriviaCategory(
            id = "food",
            title = "Treats & Dining",
            description = "Beloved snacks and restaurants from across the kingdom.",
            accentColor = 0xFF7CE29D,
        ),
        TriviaCategory(
            id = "history",
            title = "Disney History",
            description = "Open-day facts, inspiration, and stories behind iconic details.",
            accentColor = 0xFF9B8BFF,
        ),
    )

    private val questions = listOf(
        question(
            id = "lands_1",
            categoryId = "lands",
            prompt = "Which land do guests enter immediately after passing through Cinderella Castle?",
            answers = listOf(
                "Fantasyland" to true,
                "Liberty Square" to false,
                "Adventureland" to false,
                "Frontierland" to false,
            ),
            explanation = "The central walkway beyond the castle leads straight into Fantasyland.",
            difficulty = Difficulty.EASY,
        ),
        question(
            id = "lands_2",
            categoryId = "lands",
            prompt = "Which land is home to the Jungle Cruise and Pirates of the Caribbean?",
            answers = listOf(
                "Adventureland" to true,
                "Tomorrowland" to false,
                "Fantasyland" to false,
                "Liberty Square" to false,
            ),
            explanation = "Adventureland blends tropical exploration with swashbuckling pirate stories.",
            difficulty = Difficulty.EASY,
        ),
        question(
            id = "lands_3",
            categoryId = "lands",
            prompt = "What themed land sits between Fantasyland and Frontierland?",
            answers = listOf(
                "Liberty Square" to true,
                "Main Street, U.S.A." to false,
                "Adventureland" to false,
                "Tomorrowland" to false,
            ),
            explanation = "Liberty Square acts as the colonial-era bridge between the two larger lands.",
            difficulty = Difficulty.MEDIUM,
        ),
        question(
            id = "lands_4",
            categoryId = "lands",
            prompt = "Which land is anchored by TRON Lightcycle / Run and Space Mountain?",
            answers = listOf(
                "Tomorrowland" to true,
                "Fantasyland" to false,
                "Adventureland" to false,
                "Frontierland" to false,
            ),
            explanation = "Tomorrowland is the park's sci-fi corner, with both futuristic coasters.",
            difficulty = Difficulty.EASY,
        ),
        question(
            id = "lands_5",
            categoryId = "lands",
            prompt = "Which land is most closely associated with the American frontier and Big Thunder Mountain Railroad?",
            answers = listOf(
                "Frontierland" to true,
                "Liberty Square" to false,
                "Adventureland" to false,
                "Fantasyland" to false,
            ),
            explanation = "Frontierland celebrates the mythic Old West, with Big Thunder as its mountain landmark.",
            difficulty = Difficulty.EASY,
        ),
        question(
            id = "rides_1",
            categoryId = "rides",
            prompt = "What is the name of the haunted estate attraction in Liberty Square?",
            answers = listOf(
                "Haunted Mansion" to true,
                "Tower of Terror" to false,
                "Phantom Manor" to false,
                "Hall of Presidents" to false,
            ),
            explanation = "The Haunted Mansion is the spooky fan favorite overlooking Liberty Square.",
            difficulty = Difficulty.EASY,
        ),
        question(
            id = "rides_2",
            categoryId = "rides",
            prompt = "Which attraction shrinks guests down for a musical boat ride through global scenes?",
            answers = listOf(
                "it's a small world" to true,
                "Peter Pan's Flight" to false,
                "Under the Sea - Journey of The Little Mermaid" to false,
                "Buzz Lightyear's Space Ranger Spin" to false,
            ),
            explanation = "The cheerful dolls and international music make it's a small world unmistakable.",
            difficulty = Difficulty.EASY,
        ),
        question(
            id = "rides_3",
            categoryId = "rides",
            prompt = "Which coaster sends riders through a mine train with the phrase 'the wildest ride in the wilderness'?",
            answers = listOf(
                "Big Thunder Mountain Railroad" to true,
                "Seven Dwarfs Mine Train" to false,
                "Space Mountain" to false,
                "Barnstormer" to false,
            ),
            explanation = "Big Thunder's runaway mine cars have carried that famous tagline for years.",
            difficulty = Difficulty.MEDIUM,
        ),
        question(
            id = "rides_4",
            categoryId = "rides",
            prompt = "Which Tomorrowland attraction lets guests fire lasers at Emperor Zurg's targets?",
            answers = listOf(
                "Buzz Lightyear's Space Ranger Spin" to true,
                "Astro Orbiter" to false,
                "PeopleMover" to false,
                "Monsters, Inc. Laugh Floor" to false,
            ),
            explanation = "Space Ranger Spin turns every ride vehicle into a score-chasing mission.",
            difficulty = Difficulty.EASY,
        ),
        question(
            id = "rides_5",
            categoryId = "rides",
            prompt = "Which Fantasyland ride is famous for its overhead pirate ship vehicles?",
            answers = listOf(
                "Peter Pan's Flight" to true,
                "Prince Charming Regal Carrousel" to false,
                "The Many Adventures of Winnie the Pooh" to false,
                "Mad Tea Party" to false,
            ),
            explanation = "Peter Pan's Flight suspends each ship above a glowing London skyline.",
            difficulty = Difficulty.MEDIUM,
        ),
        question(
            id = "characters_1",
            categoryId = "characters",
            prompt = "Who rules from Cinderella Castle's namesake story?",
            answers = listOf(
                "Cinderella" to true,
                "Aurora" to false,
                "Belle" to false,
                "Elsa" to false,
            ),
            explanation = "Cinderella is the princess most closely tied to Magic Kingdom's iconic castle.",
            difficulty = Difficulty.EASY,
        ),
        question(
            id = "characters_2",
            categoryId = "characters",
            prompt = "Which pair of hitchhiking ghosts is NOT part of the Haunted Mansion finale?",
            answers = listOf(
                "Chip and Dale" to true,
                "Ezra and Phineas" to false,
                "Gus and Ezra" to false,
                "Phineas and Gus" to false,
            ),
            explanation = "The trio is Gus, Ezra, and Phineas. Chip and Dale are definitely not haunting the mansion.",
            difficulty = Difficulty.HARD,
        ),
        question(
            id = "characters_3",
            categoryId = "characters",
            prompt = "Which toy hero stars in Tomorrowland's shooting-gallery ride?",
            answers = listOf(
                "Buzz Lightyear" to true,
                "Woody" to false,
                "Forky" to false,
                "Bo Peep" to false,
            ),
            explanation = "Buzz leads the mission against Zurg in Space Ranger Spin.",
            difficulty = Difficulty.EASY,
        ),
        question(
            id = "characters_4",
            categoryId = "characters",
            prompt = "What mischievous alien often appears in Tomorrowland meet-and-greets and Disney's sci-fi merch?",
            answers = listOf(
                "Stitch" to true,
                "Baymax" to false,
                "Wall-E" to false,
                "Figment" to false,
            ),
            explanation = "Stitch's playful chaos makes him a natural Tomorrowland favorite.",
            difficulty = Difficulty.EASY,
        ),
        question(
            id = "characters_5",
            categoryId = "characters",
            prompt = "Which fearless adventurer captains the riverboat-style rescue in The Jungle Cruise story world?",
            answers = listOf(
                "The skippers" to true,
                "Captain Hook" to false,
                "Moana" to false,
                "Davy Jones" to false,
            ),
            explanation = "The pun-loving skippers are the stars of every Jungle Cruise voyage.",
            difficulty = Difficulty.MEDIUM,
        ),
        question(
            id = "food_1",
            categoryId = "food",
            prompt = "Which cinnamon-sugar snack is a Magic Kingdom icon in Frontierland?",
            answers = listOf(
                "Churro" to true,
                "Turkey leg" to false,
                "Dole Whip" to false,
                "Mickey waffle" to false,
            ),
            explanation = "Churros are a go-to park snack, especially near Frontierland carts.",
            difficulty = Difficulty.EASY,
        ),
        question(
            id = "food_2",
            categoryId = "food",
            prompt = "Which pineapple treat is most famously connected to Adventureland?",
            answers = listOf(
                "Dole Whip" to true,
                "Funnel cake" to false,
                "Cupcake sundae" to false,
                "School bread" to false,
            ),
            explanation = "The creamy pineapple Dole Whip is one of the park's signature desserts.",
            difficulty = Difficulty.EASY,
        ),
        question(
            id = "food_3",
            categoryId = "food",
            prompt = "Be Our Guest Restaurant is themed to which animated film?",
            answers = listOf(
                "Beauty and the Beast" to true,
                "Snow White and the Seven Dwarfs" to false,
                "The Little Mermaid" to false,
                "Tangled" to false,
            ),
            explanation = "The restaurant recreates the Beast's castle from Beauty and the Beast.",
            difficulty = Difficulty.EASY,
        ),
        question(
            id = "food_4",
            categoryId = "food",
            prompt = "Which breakfast item is shaped like Mickey Mouse at many Disney dining spots?",
            answers = listOf(
                "Waffle" to true,
                "Pretzel" to false,
                "Croissant" to false,
                "Biscuit" to false,
            ),
            explanation = "Mickey waffles are a Disney breakfast tradition and a photo favorite.",
            difficulty = Difficulty.EASY,
        ),
        question(
            id = "food_5",
            categoryId = "food",
            prompt = "Which Liberty Square restaurant is known for family-style Thanksgiving-inspired comfort food?",
            answers = listOf(
                "Liberty Tree Tavern" to true,
                "Crystal Palace" to false,
                "Tony's Town Square Restaurant" to false,
                "Skipper Canteen" to false,
            ),
            explanation = "Liberty Tree Tavern serves colonial-inspired comfort food in Liberty Square.",
            difficulty = Difficulty.MEDIUM,
        ),
        question(
            id = "history_1",
            categoryId = "history",
            prompt = "What year did Magic Kingdom open at Walt Disney World?",
            answers = listOf(
                "1971" to true,
                "1955" to false,
                "1964" to false,
                "1982" to false,
            ),
            explanation = "Magic Kingdom opened on October 1, 1971 as the first Walt Disney World theme park.",
            difficulty = Difficulty.EASY,
        ),
        question(
            id = "history_2",
            categoryId = "history",
            prompt = "Which California park inspired many layout ideas later used in Magic Kingdom?",
            answers = listOf(
                "Disneyland" to true,
                "Disney California Adventure" to false,
                "EPCOT" to false,
                "Disneyland Paris" to false,
            ),
            explanation = "Magic Kingdom takes many cues from Walt Disney's original Disneyland in Anaheim.",
            difficulty = Difficulty.EASY,
        ),
        question(
            id = "history_3",
            categoryId = "history",
            prompt = "Which company founder dreamed of building a vacation kingdom in Florida before his death?",
            answers = listOf(
                "Walt Disney" to true,
                "Roy O. Disney" to false,
                "Ub Iwerks" to false,
                "Marty Sklar" to false,
            ),
            explanation = "Walt Disney conceived the Florida project, while Roy helped make it a reality.",
            difficulty = Difficulty.EASY,
        ),
        question(
            id = "history_4",
            categoryId = "history",
            prompt = "Which Magic Kingdom transportation system circles the park entrance and resorts?",
            answers = listOf(
                "Monorail" to true,
                "PeopleMover" to false,
                "Skyliner" to false,
                "Steam train" to false,
            ),
            explanation = "The Walt Disney World Monorail is one of the resort's most recognizable icons.",
            difficulty = Difficulty.EASY,
        ),
        question(
            id = "history_5",
            categoryId = "history",
            prompt = "Who helped complete and dedicate Walt Disney World after Walt's passing?",
            answers = listOf(
                "Roy O. Disney" to true,
                "Michael Eisner" to false,
                "Bob Iger" to false,
                "Card Walker" to false,
            ),
            explanation = "Roy O. Disney delayed retirement to oversee the resort's completion and opening.",
            difficulty = Difficulty.MEDIUM,
        ),
    )

    fun categories(): List<TriviaCategory> = categories

    fun categoryById(categoryId: String): TriviaCategory =
        categories.first { it.id == categoryId }

    fun questionsFor(categoryId: String): List<TriviaQuestion> =
        questions.filter { it.categoryId == categoryId }

    private fun question(
        id: String,
        categoryId: String,
        prompt: String,
        answers: List<Pair<String, Boolean>>,
        explanation: String,
        difficulty: Difficulty,
    ) = TriviaQuestion(
        id = id,
        categoryId = categoryId,
        prompt = prompt,
        choices = answers.map { (text, isCorrect) -> TriviaChoice(text = text, isCorrect = isCorrect) },
        explanation = explanation,
        difficulty = difficulty,
    )
}

