package com.example.data

import androidx.compose.ui.graphics.Color

enum class AiToolId {
    YOUTUBE_SEO_TAGS,
    YOUTUBE_SCRIPT,
    IMAGE_PROMPT_CRAFTER,
    REELS_SHORTS_HOOKS,
    PRODUCT_AD_COPY,
    AI_TRANSLATOR_POLISHER,
    EMAIL_SPONSORSHIP_PITCH,
    CREATIVE_STORY_WRITER
}

data class AiTool(
    val id: AiToolId,
    val title: String,
    val subtitle: String,
    val placeholder: String,
    val emojiIcon: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val samplePrompts: List<String>,
    val systemPromptTemplate: (input: String, extraArg: String?) -> String
) {
    companion object {
        val ALL: List<AiTool> = listOf(
            AiTool(
                id = AiToolId.YOUTUBE_SEO_TAGS,
                title = "YouTube SEO & Viral Tags",
                subtitle = "3 High-CTR titles, SEO description, tags & hashtags",
                placeholder = "Enter video topic or concept...",
                emojiIcon = "📈",
                primaryColor = Color(0xFFF59E0B),
                secondaryColor = Color(0xFFEA580C),
                samplePrompts = listOf(
                    "Top 10 AI Tools that feel illegal to know in 2026",
                    "How I learned coding in 3 months with zero background",
                    "Smartphone camera cinematic photography secrets"
                ),
                systemPromptTemplate = { input, _ ->
                    """
You are an elite YouTube Growth & SEO strategist. Generate a complete, viral-optimized package for the following video concept:
Topic/Concept: "$input"

Format the response strictly with the following distinct sections:
### [TITLES]
1. [Curiosity Hook Title]
2. [High Search Volume Title]
3. [Viral Emotional Title]

### [DESCRIPTION]
A compelling 2-3 paragraph SEO description with keyword-rich summary, video timeline outline placeholder, and call-to-action.

### [TAGS]
Comma-separated list of 25 high-search, low-competition tags.

### [HASHTAGS]
5 viral hashtags starting with #.
                    """.trimIndent()
                }
            ),
            AiTool(
                id = AiToolId.YOUTUBE_SCRIPT,
                title = "Video & YouTube Scripts",
                subtitle = "Engaging hook, intro, structured body & CTA",
                placeholder = "Enter video topic, target audience & tone...",
                emojiIcon = "🎬",
                primaryColor = Color(0xFFEF4444),
                secondaryColor = Color(0xFFDC2626),
                samplePrompts = listOf(
                    "Why 99% of people fail at habit tracking and how to fix it",
                    "A day in the life of an indie game developer",
                    "Beginner guide to personal finance & compounding"
                ),
                systemPromptTemplate = { input, _ ->
                    """
You are a master YouTube scriptwriter who scripts videos with high retention rates.
Video Brief: "$input"

Write a full structured video script including:
1. **0:00 - 0:15 Hook**: Shocking question or visual pattern interrupt.
2. **0:15 - 0:45 Intro**: Why this matters & the promise of the video.
3. **Core Content (3 Key Points)**: Step-by-step breakdown with visual/b-roll cues [Visual: ...].
4. **Conclusion & Call To Action (CTA)**: Frictionless subscribe / comment trigger.
                    """.trimIndent()
                }
            ),
            AiTool(
                id = AiToolId.IMAGE_PROMPT_CRAFTER,
                title = "AI Image Prompt Crafter",
                subtitle = "Detailed cinematic, 8K, lighting & camera prompts",
                placeholder = "Describe the image scene or character...",
                emojiIcon = "🎨",
                primaryColor = Color(0xFF8B5CF6),
                secondaryColor = Color(0xFF7C3AED),
                samplePrompts = listOf(
                    "Cyberpunk street vendor in futuristic Tokyo rain",
                    "Cozy fantasy library inside an ancient glowing hollow tree",
                    "Portrait of an astronaut discovering bioluminescent alien flowers"
                ),
                systemPromptTemplate = { input, _ ->
                    """
You are a world-class prompt engineer for state-of-the-art AI image generators (Midjourney, DALL-E 3, Flux, Imagen).
User concept: "$input"

Generate 3 production-grade prompt variations:
1. **Cinematic Realism**: Exact 35mm / 85mm lens details, volumetric lighting, Octane render, photorealistic textures.
2. **Stylized Digital Artwork**: Specific art station style, concept art, dynamic color palette, dramatic composition.
3. **Ultra-Minimalist Aesthetic**: High-contrast, studio lighting, elegant framing, editorial quality.
Include aspect ratio suggestions (e.g. --ar 16:9) and negative prompt tips.
                    """.trimIndent()
                }
            ),
            AiTool(
                id = AiToolId.REELS_SHORTS_HOOKS,
                title = "Reels & Shorts Viral Hooks",
                subtitle = "Scroll-stopping opening hooks, short script & captions",
                placeholder = "Enter your short video idea...",
                emojiIcon = "⚡",
                primaryColor = Color(0xFFEC4899),
                secondaryColor = Color(0xFFDB2777),
                samplePrompts = listOf(
                    "3 sneaky psychology tricks supermarkets use on you",
                    "The simplest morning routine to double focus",
                    "Stop editing videos the hard way: 1 trick"
                ),
                systemPromptTemplate = { input, _ ->
                    """
You are a viral short-form video architect (TikTok, Instagram Reels, YouTube Shorts).
Idea: "$input"

Format the output strictly with these sections:
### [HOOKS]
1. Hook 1 (Contrarian: "Stop doing X...")
2. Hook 2 (Curiosity gap: "Nobody talks about this...")
3. Hook 3 (FOMO/Urgency: "If you don't do this before 2027...")

### [SCRIPT]
Fast-paced 30-45 second spoken script with fast on-screen text overlays in brackets [Text: ...].

### [CAPTIONS & HASHTAGS]
Engaging caption with discussion question + 8 targeted viral hashtags.
                    """.trimIndent()
                }
            ),
            AiTool(
                id = AiToolId.PRODUCT_AD_COPY,
                title = "Product Ad & Description",
                subtitle = "High-converting e-commerce descriptions & ad copy",
                placeholder = "Enter product name, key features & target buyers...",
                emojiIcon = "🛍️",
                primaryColor = Color(0xFF10B981),
                secondaryColor = Color(0xFF059669),
                samplePrompts = listOf(
                    "Ergonomic bamboo wrist rest for mechanical keyboard enthusiasts",
                    "Organic cold-brew coffee concentrate with zero acidity",
                    "Minimalist anti-theft waterproof travel backpack"
                ),
                systemPromptTemplate = { input, _ ->
                    """
You are a direct-response copywriting legend (David Ogilvy & Eugene Schwartz style).
Product info: "$input"

Deliver:
1. **Attention-Grabbing Headline**: Punchy benefit-first angle.
2. **Product Story & Problem Agitation**: Relatable pain point that this product dissolves.
3. **Key Feature-to-Benefit Bullets**: 4 crisp bullets converting features into emotional wins.
4. **Social Media Ad Variants**:
   - Short Facebook/Instagram Feed Ad (Problem -> Agitate -> Solution)
   - TikTok/Reels Video Ad Hook & Script
5. **Urgency Call-To-Action (CTA)**.
                    """.trimIndent()
                }
            ),
            AiTool(
                id = AiToolId.AI_TRANSLATOR_POLISHER,
                title = "AI Translator & Polisher",
                subtitle = "Translate and refine text into natural, fluent language",
                placeholder = "Paste text to translate and polish...",
                emojiIcon = "🌐",
                primaryColor = Color(0xFF3B82F6),
                secondaryColor = Color(0xFF2563EB),
                samplePrompts = listOf(
                    "We are launching our new product line tomorrow and hope you can join our livestream.",
                    "Thank you for reaching out. Let me check the schedule and get back to you shortly.",
                    "This video took me 40 hours to edit, I really hope you find it inspiring and helpful."
                ),
                systemPromptTemplate = { input, targetLang ->
                    val lang = if (targetLang.isNullOrBlank()) "English" else targetLang
                    """
You are a master polyglot linguist. Translate the following text into $lang.
Original text: "$input"

Provide ONLY the translated text. DO NOT include any explanations, JSON wrappers, notes, or metadata.
                    """.trimIndent()
                }
            ),
            AiTool(
                id = AiToolId.EMAIL_SPONSORSHIP_PITCH,
                title = "Professional Email & Sponsorship Pitch",
                subtitle = "Brand collaboration pitches & business emails",
                placeholder = "Enter brand name, your channel niche & collaboration idea...",
                emojiIcon = "✉️",
                primaryColor = Color(0xFF06B6D4),
                secondaryColor = Color(0xFF0891B2),
                samplePrompts = listOf(
                    "Tech review channel with 50K subscribers pitching NordVPN",
                    "Fitness & lifestyle creator proposing 3-part series to protein snack brand",
                    "Design tutorial channel pitching Figma integration"
                ),
                systemPromptTemplate = { input, _ ->
                    """
You are a veteran talent manager who negotiates 5-figure creator brand sponsorships.
Pitch Details: "$input"

Write two ready-to-send email pitches:
### Option 1: Direct Cold Pitch
- Subject Line: High open-rate curiosity subject
- Opening: Authentic compliment showing familiarity with the brand
- Value Proposition: Your audience demographics & why they convert for this brand
- Proposed Deliverables: (e.g. 60s dedicated integration + Reel)
- Frictionless Next Step CTA

### Option 2: Relationship Warm Follow-Up
- Shorter, relationship-focused pitch for agency rep or marketing manager.
                    """.trimIndent()
                }
            ),
            AiTool(
                id = AiToolId.CREATIVE_STORY_WRITER,
                title = "Creative Story & Narrative Writer",
                subtitle = "Captivating stories, moral tales & video narratives",
                placeholder = "Enter story characters, setting & theme...",
                emojiIcon = "📖",
                primaryColor = Color(0xFFF97316),
                secondaryColor = Color(0xFFEA580C),
                samplePrompts = listOf(
                    "An old clockmaker in Prague discovers a clock that ticks backwards",
                    "A street dog and a runaway robot navigating a neon rainstorm",
                    "A young apprentice who can hear the whispers of ancient gemstones"
                ),
                systemPromptTemplate = { input, _ ->
                    """
You are an award-winning creative author and screenwriter.
Story Concept: "$input"

Write an immersive, beautifully paced narrative featuring:
1. **Title & Logline**: One sentence summary.
2. **Setting the Scene**: Rich sensory atmosphere and opening hook.
3. **The Inciting Incident & Conflict**: Emotional stakes and moral dilemma.
4. **The Climax**: Dramatic confrontation or turning point.
5. **Resolution & Moral Takeaway**: Poetic ending that resonates with the audience.
                    """.trimIndent()
                }
            )
        )

        fun findById(id: AiToolId): AiTool {
            return ALL.firstOrNull { it.id == id } ?: ALL.first()
        }
    }
}
