package com.example.model

enum class DifficultyLevel(val label: String, val ageGroup: String, val multiplier: Float) {
    EASY("Easy", "Ages 4-6", 1.0f),
    MEDIUM("Medium", "Ages 7-9", 1.5f),
    HARD("Hard", "Ages 10-12", 2.0f),
    EXPERT("Expert", "Advanced Challenges", 2.5f)
}

data class BrainRank(
    val rankIndex: Int,
    val title: String,
    val minScore: Int,
    val icon: String,
    val perk: String,
    val colorHex: Long
)

object BrainRanks {
    val all = listOf(
        BrainRank(0, "Beginner", 0, "🌱", "Start of the brain journey", 0xFF64748B),
        BrainRank(1, "Rookie Thinker", 200, "🐣", "Unlocked custom badges", 0xFF3B82F6),
        BrainRank(2, "Brain Explorer", 500, "🧭", "Double coin bonus in practice", 0xFF10B981),
        BrainRank(3, "Smart Learner", 1000, "💡", "Unlocked expert mode teasers", 0xFFF59E0B),
        BrainRank(4, "Logic Expert", 2000, "🧩", "New secret avatar unlocked", 0xFF8B5CF6),
        BrainRank(5, "Memory Master", 3500, "🧠", "+25% XP on memory games", 0xFFEC4899),
        BrainRank(6, "Focus Hero", 5500, "🎯", "Golden crown profile border", 0xFF06B6D4),
        BrainRank(7, "Brain Champion", 8000, "🏆", "Champion certificate unlocked", 0xFFF97316),
        BrainRank(8, "Genius Master", 11000, "⚡", "Master trophy badge", 0xFF6366F1),
        BrainRank(9, "Ultimate Brain Hero", 15000, "👑", "Legendary Hero Certificate", 0xFFFFD700)
    )

    fun getRankForScore(score: Int): BrainRank {
        return all.lastOrNull { score >= it.minScore } ?: all.first()
    }
}

data class ChildAvatar(
    val id: String,
    val name: String,
    val emoji: String,
    val bgHex: Long
)

object Avatars {
    val list = listOf(
        ChildAvatar("brain_hero", "Brain Hero", "🧠", 0xFF6366F1),
        ChildAvatar("super_fox", "Super Fox", "🦊", 0xFFF97316),
        ChildAvatar("astronaut", "Astronaut", "🚀", 0xFF0EA5E9),
        ChildAvatar("magic_unicorn", "Unicorn", "🦄", 0xFFEC4899),
        ChildAvatar("dino_explorer", "Dino", "🦖", 0xFF10B981),
        ChildAvatar("robo_buddy", "Robo Buddy", "🤖", 0xFF8B5CF6),
        ChildAvatar("wise_owl", "Wise Owl", "🦉", 0xFFF59E0B),
        ChildAvatar("mighty_lion", "Mighty Lion", "🦁", 0xFFE11D48)
    )

    fun getById(id: String): ChildAvatar {
        return list.firstOrNull { it.id == id } ?: list.first()
    }
}

enum class GameCategory(val displayName: String, val icon: String, val colorHex: Long) {
    ALL("All Games", "🎮", 0xFF4F46E5),
    MEMORY("Memory Games", "🧠", 0xFFEC4899),
    FOCUS("Focus Games", "🎯", 0xFF0EA5E9),
    LOGIC("Logic Games", "🧩", 0xFF8B5CF6),
    IQ("IQ Training", "💡", 0xFFF59E0B),
    MATH("Mental Math", "🔢", 0xFF10B981)
}

data class GameItem(
    val id: String,
    val title: String,
    val category: GameCategory,
    val description: String,
    val iconEmoji: String,
    val recommendedAge: String,
    val colorHex: Long,
    val isChallenge: Boolean = false
)

object GameCatalog {
    val allGames = listOf(
        GameItem("memory_cards", "Find The Pair", GameCategory.MEMORY, "Flip cards and remember cute character pairs", "🃏", "4-12", 0xFFEC4899),
        GameItem("memory_sequence", "Memory Sequence", GameCategory.MEMORY, "Watch and repeat the sparkling color patterns", "🌈", "5-12", 0xFFF43F5E),
        GameItem("color_memory", "Color Memory", GameCategory.MEMORY, "Remember which color flashed first and repeat", "🎨", "4-9", 0xFFD946EF),
        GameItem("missing_object", "Missing Object", GameCategory.MEMORY, "Spot which object vanished from the tray", "🕵️", "5-12", 0xFFA855F7),
        GameItem("focus_challenge", "Focus Challenge", GameCategory.FOCUS, "Fast reaction test! Tap on the target, avoid traps", "⚡", "4-12", 0xFF0EA5E9, isChallenge = true),
        GameItem("speed_match", "Speed Match", GameCategory.FOCUS, "Compare fast! Do current and previous symbols match?", "⏱️", "6-12", 0xFF0284C7),
        GameItem("spot_object", "Spot The Object", GameCategory.FOCUS, "Find the hidden or different icon among the crowd", "🔍", "4-10", 0xFF06B6D4),
        GameItem("visual_tracking", "Visual Tracking", GameCategory.FOCUS, "Follow the moving star and tap its final cup", "⭐", "4-11", 0xFF14B8A6),
        GameItem("logic_path", "Logic Path", GameCategory.LOGIC, "Connect arrows and tiles to guide the star to the goal", "🛤️", "6-12", 0xFF8B5CF6),
        GameItem("pattern_logic", "Pattern Logic", GameCategory.LOGIC, "Complete the geometric and color sequences", "🧩", "5-12", 0xFF7C3AED),
        GameItem("maze_challenge", "Maze Challenge", GameCategory.LOGIC, "Solve path labyrinths with logic checkpoints", "🌀", "5-12", 0xFF6D28D9),
        GameItem("visual_iq", "Visual IQ Kids", GameCategory.IQ, "Analyze shape relations, shadow matching, and rotations", "💡", "6-12", 0xFFF59E0B),
        GameItem("odd_one_out", "Odd One Out", GameCategory.IQ, "Find the object that doesn't belong to the group", "🎭", "4-9", 0xFFD97706),
        GameItem("shadow_match", "Shadow Matching", GameCategory.IQ, "Pick the exact matching dark silhouette", "👥", "4-8", 0xFFB45309),
        GameItem("quick_counting", "Quick Counting", GameCategory.MATH, "Count colorful gems and candies at lightning speed", "🍭", "4-8", 0xFF10B981),
        GameItem("mental_math", "Mental Math Kids", GameCategory.MATH, "Quick addition and subtraction challenges with streaks", "➕", "6-12", 0xFF059669),
        GameItem("missing_number", "Missing Number", GameCategory.MATH, "Discover which number completes the arithmetic line", "🔢", "6-12", 0xFF047857),
        GameItem("multiplayer_battle", "Local 2P Battle", GameCategory.ALL, "Head-to-head split screen 2-player brain tap challenge!", "⚔️", "All Ages", 0xFFE11D48)
    )
}

data class LessonItem(
    val id: String,
    val title: String,
    val category: String,
    val iconEmoji: String,
    val readTime: String,
    val colorHex: Long,
    val summary: String,
    val contentSteps: List<String>,
    val kidTips: String
)

object LessonCatalog {
    val lessons = listOf(
        LessonItem(
            id = "lesson_memory_1",
            title = "Super Memory Tricks",
            category = "Memory Training",
            iconEmoji = "🧠",
            readTime = "3 min",
            colorHex = 0xFFEC4899,
            summary = "Learn how your brain stores memories using fun stories and colorful pictures!",
            contentSteps = listOf(
                "Step 1: Turn numbers or words into funny pictures. For example, turn '2' into a swimming duck!",
                "Step 2: Group things together into chunks. Instead of remembering 7, 4, 2, remember 74 and 2.",
                "Step 3: Repeat it out loud with rhythm, like a song or rap!",
                "Step 4: Connect items like a silly story chain: 'The duck jumped on a rainbow eating a cookie!'"
            ),
            kidTips = "Brain Tip: Always practice for 5 minutes every day. Consistency makes neural connections super strong!"
        ),
        LessonItem(
            id = "lesson_focus_1",
            title = "Laser Beam Focus",
            category = "Attention & Focus",
            iconEmoji = "🎯",
            readTime = "2 min",
            colorHex = 0xFF0EA5E9,
            summary = "Discover how to ignore distractions and keep your attention super sharp.",
            contentSteps = listOf(
                "Step 1: The One-Object Game: Look at a single toy for 30 seconds without looking away.",
                "Step 2: Take three deep balloon breaths before starting a tough puzzle.",
                "Step 3: Turn off background noise like loud TVs when you want to solve puzzles.",
                "Step 4: Notice details: Count corners, look for small hidden symbols."
            ),
            kidTips = "Focus Tip: If your mind wanders, gently bring it back without getting upset. That's how focus muscles grow!"
        ),
        LessonItem(
            id = "lesson_logic_1",
            title = "The Logic Detective",
            category = "Logic & Problem Solving",
            iconEmoji = "🕵️",
            readTime = "3 min",
            colorHex = 0xFF8B5CF6,
            summary = "Think like Sherlock Holmes! Break big problems into easy clues.",
            contentSteps = listOf(
                "Step 1: Observe what is known and what is missing.",
                "Step 2: Elimination method: Cross out answers that are definitely impossible.",
                "Step 3: Look for recurring patterns: Does it alternate A-B-A-B or grow 1-2-3?",
                "Step 4: Test your hypothesis: If you think it's a triangle, check if it fits every clue!"
            ),
            kidTips = "Logic Secret: Every puzzle is like a locked treasure chest. There is always a key hidden in the clues!"
        ),
        LessonItem(
            id = "lesson_math_1",
            title = "Lightning Fast Math",
            category = "Mental Math",
            iconEmoji = "⚡",
            readTime = "3 min",
            colorHex = 0xFF10B981,
            summary = "Speedy tricks to add and subtract numbers in your head like a calculator.",
            contentSteps = listOf(
                "Step 1: Make Friends of 10: 9+1, 8+2, 7+3, 6+4, 5+5. Always look to make a 10 first!",
                "Step 2: Adding 9 is easy: Just add 10 and subtract 1! (e.g., 15 + 9 = 25 - 1 = 24)",
                "Step 3: Doubles power: If you know 6+6=12, then 6+7 is just 12+1=13!",
                "Step 4: Visualize counting beads or stairs in your head moving up and down."
            ),
            kidTips = "Math Tip: Speed comes after accuracy. Start carefully, and soon your brain will do it instantly!"
        ),
        LessonItem(
            id = "lesson_brain_1",
            title = "How Your Amazing Brain Works",
            category = "Brain Development",
            iconEmoji = "💡",
            readTime = "4 min",
            colorHex = 0xFFF59E0B,
            summary = "Meet the billions of tiny brain cells (neurons) that grow stronger whenever you learn something new!",
            contentSteps = listOf(
                "Fact 1: Your brain has around 86 billion neurons talking to each other with tiny spark messages!",
                "Fact 2: Neuroplasticity means every time you practice a hard game, your brain builds new bridges!",
                "Fact 3: Water, healthy fruits, and good sleep are fuel that clean and recharge your brain.",
                "Fact 4: Mistakes are not failures—they are when your brain works hardest to grow!"
            ),
            kidTips = "Hero Secret: You can become smarter every single day with curiosity and practice!"
        )
    )
}
