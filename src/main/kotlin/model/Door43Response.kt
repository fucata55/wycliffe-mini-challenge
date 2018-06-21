import com.squareup.moshi.Json

data class Door43Response(
        val catalogs: List<Door43Catalog>,
        val languages: List<Door43Language>
)

data class Door43Catalog(
        val identifier: String,
        val modified:  String,
        val url:       String
)

data class Door43Language(
        val category_labels: Any,   // leaving this as an Any since it isn't needed for now
        val direction: String,      // language direction
        val identifier: String,     // language slug
        val resources: List<Door43Resource>, // list of resources from this language
        val title: String,          // language title
        @Json(name = "versification_labels") val versificationLabels: Any // not currently used
)

// a language resource
data class Door43Resource(
        val checking: Any,                // currently unused
        val comment: String,              // any comments on the resource
        val contributor: List<String>,    // list of contributors to the resource
        val creator: String,              // resource creator/owner
        val description: String,          // resource description
        val formats: List<Door43Format>,        // formats for accessing the complete resource
        val identifier: String,           // resource slug
        val issued: String,               // create date
        val modified: String,             // last modified date
        val projects: List<Door43Project>,      // list of resource projects
        val publisher: String,            // resource publisher
        val relation: List<Any>,          // unneeded for now
        val rights: String,               // resource licensing
        val source: List<Any>,            // list of sources, unneeded for now
        val subject: String,              // resource subject
        val title: String,                // resource title
        val version: String               // resource version string
)

// a format to access a resource/project
data class Door43Format(
        val format: String,               // file format of the target
        val modified: String,             // last modified date of target
        val signature: String,            // target's signature url
        val size: Long,                   // target's size (in bytes)
        val url: String                   // access url for target
)

// a project within a language resource
data class Door43Project(
        val categories: List<String>,
        val formats: List<Door43Format>,
        val identifier: String,
        val sort: Int,
        val title: String,
        val versification: String
)