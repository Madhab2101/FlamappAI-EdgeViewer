#ifndef FLAMAPPAI_OPENCV_PROCESSOR_HPP
#define FLAMAPPAI_OPENCV_PROCESSOR_HPP

#include <vector>
#include <cstdint>

namespace flamappai {
    void init(int w, int h);
    void release();
    void processFrame(const uint8_t* rgba, int mode);
    const std::vector<uint8_t>& getOutput();
}

#endif // FLAMAPPAI_OPENCV_PROCESSOR_HPP