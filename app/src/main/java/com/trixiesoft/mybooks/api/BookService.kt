package com.trixiesoft.mybooks.api
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.android.parcel.Parcelize
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class BookAPI {
    companion object {
        private fun create(): BookService {

            val logging = HttpLoggingInterceptor()
            // set your desired log level
            logging.level = HttpLoggingInterceptor.Level.BODY

            val httpClient = OkHttpClient.Builder().addInterceptor(logging)

            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://openlibrary.org/")
                .client(httpClient.build())
                .build()
            return retrofit.create(BookService::class.java)
        }

        val instance by lazy { create() }
    }

    interface BookService {
        @GET("search.json")
        fun searchBooksByTitle(@Query("title") title: String): Single<BookResult>

        @GET("search.json")
        fun searchBooksByAuthor(@Query("author") author: String): Single<BookResult>

        // Example URLs
        // http://openlibrary.org/search.json?title=the+lord+of+the+rings
        // http://openlibrary.org/search.json?author=tolkien
    }
}

data class BookResult(
    @SerializedName("docs") val books: List<Book>,
    //@SerializedName("numFound") val numFound: Int,
    @SerializedName("num_found") val numFound: Int,
    @SerializedName("start") val start: Int
)

fun Book.getCoverUrlSmall(): String? {
    if (coverI != null) {
        //return String.format("http://covers.openlibrary.org/b/id/%s-S.jpg?default=false", coverI)
        return String.format("http://covers.openlibrary.org/b/id/%s-S.jpg?default=false", coverI)
    }
    return null
}

fun Book.getCoverUrlMedium(): String? {
    if (coverI != null) {
        return String.format("http://covers.openlibrary.org/b/id/%s-M.jpg?default=false", coverI)
    }
    return null
}

fun Book.getCoverUrlLarge(): String? {
    if (coverI != null) {
        return String.format("http://covers.openlibrary.org/b/id/%s-L.jpg?default=false", coverI)
    }
    return null
}

@Parcelize
data class Book(
    @SerializedName("key") val key: String,
    @SerializedName("author_key") val authorKey: List<String>,
    @SerializedName("author_name") val authorName: List<String>,
    @SerializedName("language") val language: List<String>,
    @SerializedName("cover_i") val coverI: String?,
    @SerializedName("isbn") val isbn: List<String>?,
    @SerializedName("lccn") val lccn: List<String>?,
    @SerializedName("oclc") val oclc: List<String>?,
    @SerializedName("publish_date") val publishDate: List<String>?,
    @SerializedName("publish_place") val publishPlace: List<String>?,
    @SerializedName("publish_year") val publishYear: List<Int>?,
    @SerializedName("subject") val subject: List<String>?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("text") val text: List<String>?,
    @SerializedName("time") val time: List<String>?,
    @SerializedName("title") val title: String,
    @SerializedName("title_suggest") val titleSuggest: String?,
    @SerializedName("type") val type: String

    // ISBN, OCLC, LCCN, OLID and ID (case-insensitive)
    //@SerializedName("author_alternative_name") val authorAlternativeName: List<String>,
    //@SerializedName("contributor") val contributor: List<String>,
    //@SerializedName("cover_edition_key") val coverEditionKey: String,
    //@SerializedName("ebook_count_i") val ebookCountI: Int,
    //@SerializedName("edition_count") val editionCount: Int,
    //@SerializedName("edition_key") val editionKey: List<String>,
    //@SerializedName("first_publish_year") val firstPublishYear: Int,
    //@SerializedName("first_sentence") val firstSentence: List<String>,
    //@SerializedName("has_fulltext") val hasFulltext: Boolean,
    //@SerializedName("ia") val ia: List<String>,
    //@SerializedName("ia_box_id") val iaBoxId: List<String>,
    //@SerializedName("ia_collection_s") val iaCollectionS: String,
    //@SerializedName("ia_loaded_id") val iaLoadedId: List<String>,
    //@SerializedName("id_canadian_national_library_archive") val idCanadianNationalLibraryArchive: List<String>,
    //@SerializedName("id_depósito_legal") val idDepositoLegal: List<String>,
    //@SerializedName("id_goodreads") val idGoodreads: List<String>,
    //@SerializedName("id_google") val idGoogle: List<String>,
    //@SerializedName("id_librarything") val idLibrarything: List<String>,
    //@SerializedName("id_overdrive") val idOverdrive: List<String>,
    //@SerializedName("id_wikidata") val idWikidata: List<String>,
    //@SerializedName("last_modified_i") val lastModifiedI: Int,
    //@SerializedName("lending_edition_s") val lendingEditionS: String,
    //@SerializedName("lending_identifier_s") val lendingIdentifierS: String,
    //@SerializedName("person") val person: List<String>,
    //@SerializedName("place") val place: List<String>,
    //@SerializedName("printdisabled_s") val printdisabledS: String,
    //@SerializedName("public_scan_b") val publicScanB: Boolean,
    //@SerializedName("publisher") val publisher: List<String>,
    //@SerializedName("seed") val seed: List<String>,
): Parcelable
