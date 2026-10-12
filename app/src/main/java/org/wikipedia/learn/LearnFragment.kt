package org.wikipedia.learn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.wikipedia.R
import org.wikipedia.compose.theme.BaseTheme
import org.wikipedia.compose.theme.WikipediaTheme
import org.wikipedia.WikipediaApp
import org.wikipedia.history.HistoryEntry
import org.wikipedia.page.PageActivity
import org.wikipedia.page.PageTitle
import java.io.InputStreamReader

class LearnFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                BaseTheme {
                    LearnScreen(
                        onSelectTopic = { titleText ->
                            val pageTitle = PageTitle(titleText, WikipediaApp.instance.wikiSite)
                            val entry = HistoryEntry(pageTitle, HistoryEntry.SOURCE_INTERNAL_LINK)
                            startActivity(PageActivity.newIntentForNewTab(requireContext(), entry, pageTitle))
                        }
                    )
                }
            }
        }
    }

    companion object {
        fun newInstance(): LearnFragment {
            return LearnFragment()
        }
    }
}

data class CourseModel(
    val title: String,
    val slug: String,
    val shortDesc: String,
    val modulesCount: Int,
    val modules: List<ModuleItem>
)

data class ModuleItem(
    val num: Int,
    val title: String,
    val href: String
)

data class ContentSeed(
    val curriculum: List<CourseModel> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(
    onSelectTopic: (String) -> Unit
) {
    var courses by remember { mutableStateOf<List<CourseModel>>(emptyList()) }
    var selectedCourse by remember { mutableStateOf<CourseModel?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        try {
            val stream = context.assets.open("seed/content.json")
            val reader = InputStreamReader(stream)
            val jsonObject = com.google.gson.JsonParser.parseReader(reader).asJsonObject
            val curriculumArray = jsonObject.getAsJsonArray("curriculum")
            val list = mutableListOf<CourseModel>()

            curriculumArray.forEach { item ->
                val obj = item.asJsonObject
                val title = obj.get("title").asString
                val slug = obj.get("slug").asString
                val shortDesc = if (obj.has("shortDesc")) obj.get("shortDesc").asString else ""
                val count = if (obj.has("modulesCount")) obj.get("modulesCount").asInt else 0
                val mods = mutableListOf<ModuleItem>()

                if (obj.has("curriculumModules")) {
                    obj.getAsJsonArray("curriculumModules").forEach { m ->
                        val mObj = m.asJsonObject
                        mods.add(
                            ModuleItem(
                                num = mObj.get("num").asInt,
                                title = mObj.get("title").asString,
                                href = if (mObj.has("href")) mObj.get("href").asString else ""
                            )
                        )
                    }
                }
                list.add(CourseModel(title, slug, shortDesc, count, mods))
            }
            reader.close()
            courses = list
        } catch (e: Exception) {
            // Fallback default list
            courses = listOf(
                CourseModel("Programming Fundamentals", "programming-fundamentals", "Variables, control flow, functions, loops, and problem-solving patterns.", 6, emptyList()),
                CourseModel("Object-Oriented Programming", "object-oriented-programming", "Encapsulation, inheritance, polymorphism, and modular software design.", 6, emptyList()),
                CourseModel("Data Structures", "data-structures", "Arrays, linked lists, stacks, queues, trees, graphs, and hash tables.", 6, emptyList()),
                CourseModel("Digital Logic Design", "digital-logic-design", "Binary arithmetic, Boolean logic, logic gates, and combinational circuits.", 6, emptyList()),
                CourseModel("Database Management Systems", "database-management-systems", "Relational algebra, SQL, normalization, transactions, and indexing.", 6, emptyList()),
                CourseModel("Software Engineering", "software-engineering", "Lifecycle models, architectural patterns, design principles, testing, and CI/CD.", 6, emptyList()),
                CourseModel("Artificial Intelligence", "artificial-intelligence", "Search algorithms, heuristics, knowledge representation, and neural networks.", 6, emptyList())
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedCourse != null) selectedCourse!!.title else "Curriculum",
                        fontWeight = FontWeight.Bold,
                        color = WikipediaTheme.colors.primaryColor
                    )
                },
                navigationIcon = {
                    if (selectedCourse != null) {
                        IconButton(onClick = { selectedCourse = null }) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(R.drawable.ic_arrow_back_black_24dp),
                                contentDescription = "Back",
                                tint = WikipediaTheme.colors.primaryColor
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WikipediaTheme.colors.paperColor
                )
            )
        },
        containerColor = WikipediaTheme.colors.paperColor
    ) { paddingValues ->
        if (selectedCourse != null) {
            // Syllabus view
            val course = selectedCourse!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = course.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = WikipediaTheme.colors.primaryColor
                    )
                    Text(
                        text = course.shortDesc,
                        fontSize = 14.sp,
                        color = WikipediaTheme.colors.secondaryColor,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )
                    Divider(color = WikipediaTheme.colors.borderColor)
                }

                items(course.modules) { mod ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(WikipediaTheme.colors.paperColor)
                            .border(1.dp, WikipediaTheme.colors.borderColor, RoundedCornerShape(8.dp))
                            .clickable { onSelectTopic(mod.title) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(WikipediaTheme.colors.backgroundColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${mod.num}",
                                fontWeight = FontWeight.Bold,
                                color = WikipediaTheme.colors.primaryColor
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = mod.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = WikipediaTheme.colors.primaryColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        } else {
            // Course list view
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Computer Science Track",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = WikipediaTheme.colors.primaryColor
                    )
                    Text(
                        text = "Rigorous, structured computer science foundations from first principles.",
                        fontSize = 14.sp,
                        color = WikipediaTheme.colors.secondaryColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Divider(color = WikipediaTheme.colors.borderColor, modifier = Modifier.padding(top = 12.dp))
                }

                items(courses) { course ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(WikipediaTheme.colors.paperColor)
                            .border(1.dp, WikipediaTheme.colors.borderColor, RoundedCornerShape(8.dp))
                            .clickable { selectedCourse = course }
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "${course.modulesCount} MODULES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = WikipediaTheme.colors.progressiveColor,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = course.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = WikipediaTheme.colors.primaryColor,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = course.shortDesc,
                            fontSize = 13.sp,
                            color = WikipediaTheme.colors.secondaryColor,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
