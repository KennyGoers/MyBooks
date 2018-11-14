package com.trixiesoft.mybooks

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.trixiesoft.mybooks.db.AppDatabase
import com.trixiesoft.mybooks.db.Book
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
public class AppDatabaseTest {
    private var db: AppDatabase? = null

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db!!.close()
    }

    @Test
    @Throws(Exception::class)
    fun addGetBook() {
        db?.dao?.insertBooks(Book(0, "9789402306538", "The Hobbit", "J. R. R. Tolkien", false))

        val books = db?.dao?.getBooks()
        assertEquals(books?.size, 1)

        /*
        // When subscribing to the emissions of the user
        dao.getAllVehiclesFlowable()
            .test()
            .await(5, TimeUnit.SECONDS)
            .assertValue(vehicles -> {
                // The emitted list is the same as written
                return vehicles != null && TestData.getNetworkVehiclesImmutable().size() ==  vehicles.size();
            });
            */
    }
}
