JComic
======

Support Android 2.3.3+

JComic is a comic book and manga reader aimed at users who like to read comics on their android devices.

What sets JComic apart from other image viewers is its focus on comics, as well as its customizability; we all have different reading habits, different devices, different sized screens and different versions of Android. It goes without saying that one configuration will never suit everybody. 

That's where JComic comes in.

While the default settings are appropriate for most users, JComic allows those who know what they want from their devices to customize the app to suit their preferences, from caching to forced screen orientation to remembering frequented directories and so on.


JComic currently supports the following formats.

Images: .jpg, .jpeg, .png, .gif, .webp (Android 4.0+)

Archives: .zip, .cbz, .rar, .cbr, .tar, .ar
(support for .tar.bz2, .tar.gz and .tar.xz included, but never implemented)



gnu.crypto contains utilities for rar password decryption
de.innosystec.unrar contains utilities for utilizing the gnu.crypto library
If you plan on implementing support for password-protected rar files, you'll need to use those two libraries.
This has been partially implemented in the back-end, but I never provided a UI element for doing so.
You can pass in a password argument to ArchiveParser.parseArchive(File, Password)

If you do NOT plan on using rar decryption at all, you can remove both libraries and instead use junrar
in order to reduce the size of the resulting project and speed things up slightly.