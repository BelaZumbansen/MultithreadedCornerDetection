# Multithreaded-Corner-Detection
Problem Description:
The FAST algorithm (Rosten & Drummond, 2006) is a heuristic way to detect corners within in an image.
The approach requires inspecting the pixels on the circumference of a small circle (e.g., based on a radius
of 3) around a potential corner. If a large, contiguous (along the circumference) number of them are
significantly brighter (or a large, contiguous number are darker) then the center point represents a corner.
You need to develop code that goes through almost every pixel, determines whether that point represents a 
corner or not, and if so draws a corresponding circle (in red, tracing out the same circle as used for 
detection) in a copy of the original image. This work is to be done by multiple threads, and must show some
relative speedup in both the time taken to do detection and drawing. Circle drawing in the output image must
be done pixel-by-pixel (do not use built-in methods to draw it).
